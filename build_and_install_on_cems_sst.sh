git pull github master
mvn clean install package assembly:directory

rm -rf /group_workspaces/cems2/esacci_sst/mms_new/bin/*
cp -r target/fiduceo-master-1.2.9-MMS/* /group_workspaces/cems2/esacci_sst/mms_new/bin