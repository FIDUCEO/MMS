git pull github master
mvn clean install package assembly:directory

rm -rf /group_workspaces/cems2/fiduceo/Software/mms/bin/*
cp -r target/fiduceo-master-1.0.4-SNAPSHOT-MMS/* /group_workspaces/cems2/fiduceo/Software/mms/bin