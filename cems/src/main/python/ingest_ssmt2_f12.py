from workflow import Workflow

w = Workflow('ingest_ssmt2-f12', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('ssmt2-f12', '1994-09-08', '2002-07-30', 'v01')

w.run_ingestion(hosts=[('localhost', 24)])