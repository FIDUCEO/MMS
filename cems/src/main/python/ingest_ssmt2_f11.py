from workflow import Workflow

w = Workflow('ingest_ssmt2-f11', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('ssmt2-f11', '1992-04-12', '2000-05-16', 'v01')

w.run_ingestion(hosts=[('localhost', 24)])