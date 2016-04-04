git pull github master
mvn clean install package assembly:directory

rm -rf /group_workspaces/cems2/fiduceo/Software/mms/bin/*
cp target/fiduceo-master-1.0-SNAPSHOT-MMS/* /group_workspaces/cems2/fiduceo/Software/mms/bin