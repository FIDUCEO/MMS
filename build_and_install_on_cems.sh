git pull github master
mvn clean install package assembly:directory
cp target/fiduceo-master-1.0-SNAPSHOT-MMS/* /group_workspace/cems2/fiduceo/Software/mms/bin