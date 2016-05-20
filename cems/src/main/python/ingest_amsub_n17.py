from workflow import Workflow

w = Workflow('ingest_amsub_n17', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('amsub-n17', '2002-06-05', '2013-04-10', 'v1.0')

w.run_ingestion(hosts=[('localhost', 24)])