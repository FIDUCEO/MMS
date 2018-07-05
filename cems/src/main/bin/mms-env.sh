#!/bin/bash

# MMS function definitions
# useage ${mms_home}/bin/${MMS_ENV_NAME}  (in xxx-start.sh and xxx-run.sh)

# the following exports are CEMS/Fiduceo specific settings of executables to be used
# adapt if necessary tb 2016-04-03

export MMS_HOME='/group_workspaces/cems2/fiduceo/Software/mms'
export MMS_TEMP_DIR='/group_workspaces/cems2/fiduceo/Software/mms/temp'
export MMS_PYTHON_EXEC='/usr/bin/python'
export MMS_JAVA_EXEC='/group_workspaces/cems2/fiduceo/Software/jdk1.8.0_73/bin/java'
export MMS_ENV_NAME='mms-env.sh'

export PATH=$MMS_HOME/bin:$PATH

set -e

if [ -z "${MMS_INST}" ]; then
    MMS_INST=`pwd -P`
fi

MMS_TASKS=${MMS_INST}/tasks
MMS_LOG=${MMS_INST}/log

read_task_jobs() {
    jobname=$1
    jobs=
    if [ -e ${MMS_TASKS}/${jobname}.tasks ]
    then
        for logandid in `cat ${MMS_TASKS}/${jobname}.tasks`
        do
            job=`basename ${logandid}`
            log=`dirname ${logandid}`
            if grep -qF 'Successfully completed.' ${log}
            then
                if [ "${jobs}" != "" ]
                then
                    jobs="${jobs}|${job}"
                else
                    jobs="${job}"
                fi
            fi
        done
    fi
}

wait_for_task_jobs_completion() {
    jobname=$1
    while true
    do
        sleep 10
        # Output of bjobs command:
        # JOBID   USER    STAT  QUEUE      FROM_HOST   EXEC_HOST   JOB_NAME   SUBMIT_TIME
        # 619450  rquast  RUN   lotus      lotus.jc.rl host042.jc. *r.n10-sub Aug 14 10:15
        # 619464  rquast  RUN   lotus      lotus.jc.rl host087.jc. *r.n11-sub Aug 14 10:15
        # 619457  rquast  RUN   lotus      lotus.jc.rl host209.jc. *r.n12-sub Aug 14 10:15
        # 619458  rquast  RUN   lotus      lotus.jc.rl host209.jc. *r.n11-sub Aug 14 10:15
        # 619452  rquast  RUN   lotus      lotus.jc.rl host043.jc. *r.n10-sub Aug 14 10:15
        if bjobs -P fiduceo | egrep -q "^$jobs\\>"
        then
            continue
        fi

        if [ -s ${MMS_TASKS}/${jobname}.tasks ]
        then
            for logandid in `cat ${MMS_TASKS}/${jobname}.tasks`
            do
                job=`basename ${logandid}`
                log=`dirname ${logandid}`

                if [ -s ${log} ]
                then
                    if ! grep -qF 'Successfully completed.' ${log}
                    then
                        echo "tail -n10 ${log}"
                        tail -n10 ${log}
                        echo "`date -u +%Y%m%d-%H%M%S`: tasks for ${jobname} failed (reason: see ${log})"
                        exit 1
                    else
                        echo "`date -u +%Y%m%d-%H%M%S`: tasks for ${jobname} done"
                        exit 0
                    fi
                else
                        echo "`date -u +%Y%m%d-%H%M%S`: logfile ${log} for job ${job} not found"
                fi
            done
        fi
    done
}

submit_job() {
    jobname=$1
    command=$2

    bsubmit="bsub -R rusage[mem=16000] -M 16000 -q short-serial -W 12:00 -P fiduceo -cwd ${MMS_INST} -oo ${MMS_LOG}/${jobname}.out -eo ${MMS_LOG}/${jobname}.err -J ${jobname} ${MMS_HOME}/bin/${command} ${@:3}"

    rm -f ${MMS_LOG}/${jobname}.out
    rm -f ${MMS_LOG}/${jobname}.err

    echo "${bsubmit}"
    line=`${bsubmit}`

    echo ${line}
    if echo ${line} | grep -qF 'is submitted'
    then
        jobs=`echo ${line} | awk '{ print substr($2,2,length($2)-2) }'`
         # this call sets the correct start condition for the wait-procedure "wait_for_task_jobs_completion()"
        echo "${MMS_LOG}/${jobname}.out/${jobs}" > ${MMS_TASKS}/${jobname}.tasks
    else
        echo "`date -u +%Y%m%d-%H%M%S`: tasks for ${jobname} failed (reason: was not submitted)"
        exit 1
    fi
}
