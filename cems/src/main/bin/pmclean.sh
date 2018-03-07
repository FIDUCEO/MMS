#!/bin/sh

if [ -z "$1" ]; then
    echo "usage: pmclean <usecase>"
    exit 1
fi

usecase=$1

if [ -d log ]; then
  rm -rf log
fi
mkdir log

if [ -d tasks ]; then
  rm -rf tasks
fi
mkdir tasks

if [ -d trace ]; then
  rm -rf trace
fi
mkdir trace

rm -f ${usecase}.report ${usecase}.status ${usecase}.pid ${usecase}.out
