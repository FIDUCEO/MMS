#!/bin/bash
set -x
# example usage: post_processing_run.sh /archive/data 1982-234 1982-231 update_sst.xml ./config_dir

input_dir=$1
start_date=$2
end_date=$3
job_config=$4
config_dir=$5

echo "`date -u +%Y%m%d-%H%M%S` ingestion ${start_date} - ${end_date} ..."

${MMS_HOME}/bin/post-processing-tool.sh -i ${input_dir} -start ${start_date} -end ${end_date} -j ${job_config} -c ${config_dir}
