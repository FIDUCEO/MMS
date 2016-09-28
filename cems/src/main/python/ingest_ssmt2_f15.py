from workflow import Workflow

w = Workflow('ingest_ssmt2-f15', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('ssmt2-f15', '2000-08-16', '2008-05-28', 'v01')

w.run_ingestion(hosts=[('localhost', 24)])