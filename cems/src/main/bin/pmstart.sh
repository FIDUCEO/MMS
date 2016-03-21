#!/bin/bash

if [ -z "$1" ]; then
    echo "call   : pmstart <workflow>"
    echo "example: pmstart usecase-17.py"
    exit 1
fi


WORKING_DIR=`pwd`

workflow=$(basename ${1%.py})

if [ -e ${workflow}.pid ]
then
    if kill -0 $(cat $workflow.pid) 2> /dev/null
    then
        ps -elf | grep $(cat $workflow.pid) | grep -v grep
        echo "process already running"
        echo "delete $workflow.pid file if running process is not the workflow"
        exit 1
    fi
fi

# @todo 1 tb/tb check which environment variables we need at CEMS
nohup ${mms.python.exec} ${mms.home}/python/$workflow.py > $WORKING_DIR/$workflow.out 2>&1 &
echo $! > $WORKING_DIR/$workflow.pid
sleep 8
cat $WORKING_DIR/$workflow.status
