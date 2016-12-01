#!/bin/bash

. ${MMS_HOME}/bin/${MMS_ENV_NAME}

start_date=$1
end_date=$2
config_dir=$3
use_case_config=$4

task="matchup"
jobname="${task}-${use_case_config}-${start_date}-${end_date}"
echo ${jobname}

command="${task}_run.sh ${start_date} ${end_date} ${config_dir} ${use_case_config}"

echo "`date -u +%Y%m%d-%H%M%S` submitting job '${jobname}'"

read_task_jobs ${jobname}

if [ -z ${jobs} ]; then
    submit_job ${jobname} ${command}
fi

wait_for_task_jobs_completion ${jobname}