#!/usr/bin/env bash
mmspath="/group_workspaces/cems2/esacci_sst/mms_new"
binpath=$mmspath/bin
binarcpath=$mmspath/bin_archive
bintime="$(find "$binpath" -name lib -type d -printf "%CF_%CT %f\n" | cut -c -19)"
cp -a "$binpath" "$binarcpath/${binpath##*/}-$bintime"
