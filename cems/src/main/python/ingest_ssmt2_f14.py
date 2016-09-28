from workflow import Workflow

w = Workflow('ingest_ssmt2-f14', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('ssmt2-f14', '1997-04-28', '2006-10-30', 'v01')

w.run_ingestion(hosts=[('localhost', 24)])