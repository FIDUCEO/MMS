from workflow import Workflow

w = Workflow('ingest_amsub_n16', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('amsub-n16', '2000-09-21', '2014-06-05', 'v1.0')

w.run_ingestion(hosts=[('localhost', 24)])