from workflow import Workflow

w = Workflow('ingest_hirs_n16', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n16', '2001-01-01', '2014-06-05', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])