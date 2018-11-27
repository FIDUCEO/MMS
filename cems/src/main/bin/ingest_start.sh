#!/bin/bash

. ${MMS_HOME}/bin/${MMS_ENV_NAME}

sensor=$1
start_date=$2
end_date=$3
data_version=$4
config_dir=$5

task="ingest"
jobname="${task}-${sensor}-${data_version}-${start_date}-${end_date}"
echo ${jobname}

command="${task}_run.sh ${sensor} ${start_date} ${end_date} ${data_version} ${config_dir}"

echo "`date -u +%Y%m%d-%H%M%S` submitting job '${jobname}'"

read_task_jobs ${jobname}

if [ -z ${jobs} ]; then
    submit_job ${jobname} ${command}
fi