#!/usr/bin/env bash
git pull github master
mvn clean install package assembly:directory

rm -rf /group_workspaces/cems2/fiduceo/Software/mms/bin/*
cp -r target/fiduceo-master-1.4.1-MMS/* /group_workspaces/cems2/fiduceo/Software/mms/bin