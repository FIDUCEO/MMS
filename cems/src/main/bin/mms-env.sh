#!/bin/bash

# MMS function definitions
# useage ${mms_home}/bin/${MMS_ENV_NAME}  (in xxx-start.sh and xxx-run.sh)

# the following exports are CEMS/Fiduceo specific settings of executables to be used
# adapt if necessary tb 2018-11-27

# project and user settings
# -------------------------
export PROJECT=bc_fiduceo   # only LSF
export MMS_USER=tblock01    # only SLURM
export MMS_ENV_NAME=mms-env.sh

# Java and Python runtime definitions
# -----------------------------------
export MMS_JAVA_EXEC='/gws/nopw/j04/esacci_sst/mms_new/software/jdk1.8.0_202/bin/java'

export PM_EXE_DIR=/gws/nopw/j04/esacci_sst/mms_new/bin
export PM_PYTHON_EXEC='/gws/nopw/j04/esacci_sst/mms_new/software/conda_envs/sst-cci-mms/bin/python'

export PATH=${PM_EXE_DIR}:$PATH

# export scheduling engine
# ------------------------
export SCHEDULER='SLURM'
# export SCHEDULER='LSF'

# ensure that processes exit
set -e

if [ -z "${WORKING_DIR}" ]; then
    WORKING_DIR=`pwd -P`
fi

export PM_LOG_DIR=${WORKING_DIR}/log

if [ "$SCHEDULER" == "LSF" ]; then

    submit_job() {
        jobname=$1
        command=$2

        bsubmit="bsub -R rusage[mem=20000] -M 20000 -q short-serial -n 1 -W 12:00 -P ${PROJECT} -cwd ${WORKING_DIR} -oo ${PM_LOG_DIR}/${jobname}.out -eo ${PM_LOG_DIR}/${jobname}.err -J ${jobname} ${PM_EXE_DIR}/${command} ${@:3}"

        rm -f ${PM_LOG_DIR}/${jobname}.out
        rm -f ${PM_LOG_DIR}/${jobname}.err

        # line contains the console output of the bsub command
        line=`${bsubmit}`

        if echo ${line} | grep -qF 'is submitted'
        then
            # extract the job_id from the bsub message, concatenate '_' and jobname to form an identifier
            # and dump to std_out to be fetched by pmonitor
            job_id=`echo ${line} | awk '{ print substr($2,2,length($2)-2) }'`
            echo "${job_id}_${jobname}"
        else
            echo "`date -u +%Y%m%d-%H%M%S` - submit of ${jobname} failed: ${line}"
            exit 1
        fi
    }

elif [ "$SCHEDULER" == "SLURM" ]; then

    submit_job() {
            jobname=$1
            command=$2

            bsubmit="sbatch --mem=20000 -p short-serial -n 1 -t 12:00:00 -D ${WORKING_DIR} -o ${PM_LOG_DIR}/${jobname}.out -e ${PM_LOG_DIR}/${jobname}.err -J ${jobname} ${PM_EXE_DIR}/${command} ${@:3}"

            rm -f ${PM_LOG_DIR}/${jobname}.out
            rm -f ${PM_LOG_DIR}/${jobname}.err

            # line contains the console output of the bsub command
            line=`${bsubmit}`

            if echo ${line} | grep -qF 'Submitted batch job'
            then
                # extract the job_id from the bsub message, concatenate '_' and jobname to form an identifier
                # and dump to std_out to be fetched by pmonitor
                job_id=`echo ${line} | awk '{ print substr($4,0,length($4)) }'`
                echo "${job_id}_${jobname}"
            else
                echo "`date -u +%Y%m%d-%H%M%S` - submit of ${jobname} failed: ${line}"
                exit 1
            fi
        }

else
    echo "Invalid scheduler"
    exit 1
fi