#!/usr/bin/env bash
mmspath="/group_workspaces/cems2/fiduceo/Software/mms"
binpath=$mmspath/bin
binarcpath=$mmspath/bin_archive
bintime="$(find "$binpath" -name lib -type d -printf "%TF_%TT %f\n" | cut -c -19)"
echo "backup bin dir to bin_archive with timestamp $bintime"
cp "$binpath" "$binarcpath/${binpath##*/}-$bintime"
echo "... done"
