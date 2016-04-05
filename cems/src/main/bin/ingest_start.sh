#!/bin/bash

. ${MMS_HOME}/bin/mms-env.sh

sensor=$1
start_date=$2
end_date=$3
version=$4
config_dir=$5

task="ingest"
jobname="${task}-${sensor}-${version}-${start_date}-${end_date}"
echo ${jobname}

command="${task}_run.sh ${sensor} ${start_date} ${end_date} ${version} ${config_dir}"

echo "`date -u +%Y%m%d-%H%M%S` submitting job '${jobname}'"

read_task_jobs ${jobname}

if [ -z ${jobs} ]; then
    submit_job ${jobname} ${command}
fi

wait_for_task_jobs_completion ${jobname}