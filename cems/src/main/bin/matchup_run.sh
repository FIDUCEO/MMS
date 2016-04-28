#!/bin/bash
set -x
# example usage: matchup-run.sh 1982-234 1982-231 ./config_dir mms02.xml

start_date=$1
end_date=$2
config_dir=$3
use_case_config=$4

echo "`date -u +%Y%m%d-%H%M%S` matchup ${use_case_config} ${start_date} - ${end_date} ..."

${MMS_HOME}/bin/matchup-tool.sh -start ${start_date} -end ${end_date} -c ${config_dir} -u ${use_case_config}
