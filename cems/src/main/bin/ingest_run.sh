#!/bin/bash
set -x
# example usage: ingestion-run.sh avhrr-n07 1982-234 1982-231 v01.2 ./config_dir

sensor=$1
start_date=$2
end_date=$3
data_version=$4
config_dir=$5

echo "`date -u +%Y%m%d-%H%M%S` ingestion ${start_date} - ${end_date} ..."

${MMS_HOME}/bin/ingestion-tool.sh -s ${sensor} -start ${start_date} -end ${end_date} -v ${data_version} -c ${config_dir}
