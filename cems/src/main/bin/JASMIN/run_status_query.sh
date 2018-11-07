#!/bin/bash

# ensure that processes exit
set -e

python jasmin_job_monitor.py "$1"