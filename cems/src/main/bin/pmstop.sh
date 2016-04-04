#!/bin/bash

WORKING_DIR=`pwd`

if [ -z "$1" ]; then
    echo "usage  : pmstop <workflow>"
    echo "example: pmstop usecase-12.py"
    exit 1
fi

workflow=$(basename ${1%.py})

if [ ! -e $WORKING_DIR/$workflow.pid ]; then
    echo "missing $workflow.pid file in $WORKING_DIR"
    ps -elf|grep python
    exit 1
fi

kill $(cat $WORKING_DIR/$workflow.pid)