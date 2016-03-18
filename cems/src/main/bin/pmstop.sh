#!/bin/bash

# @todo 1 tb/tb check if we need this - or always assume that cwd is used 2016-03-18
if [ -z "$MMS_INST" ]; then
    MMS_INST=`pwd`
fi

if [ -z "$1" ]; then
    echo "usage  : pmstop <workflow>"
    echo "example: pmstop usecase-12.py"
    exit 1
fi

workflow=$(basename ${1%.py})

if [ ! -e $MMS_INST/$workflow.pid ]; then
    echo "missing $workflow.pid file in $MMS_INST"
    ps -elf|grep python
    exit 1
fi

kill $(cat $MMS_INST/$workflow.pid)