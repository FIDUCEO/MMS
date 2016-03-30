#!/bin/bash

. ${mms.home}/bin/mms-env.sh

sensor=$1
start_date=$2
end_date=$3
config_dir=$4

task="ingest"
jobname="${task}-${sensor}-${start_date}-${end_date}"
command="${task}_run.sh ${sensor} ${month} ${usecase}"

echo "`date -u +%Y%m%d-%H%M%S` submitting job '${jobname}'"

read_task_jobs ${jobname}

if [ -z ${jobs} ]; then
    submit_job ${jobname} ${command}
fi

wait_for_task_jobs_completion ${jobname}