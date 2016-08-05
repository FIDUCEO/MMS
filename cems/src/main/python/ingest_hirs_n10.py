from workflow import Workflow

w = Workflow('ingest_hirs_n10', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n10', '1986-11-30', '1991-09-16', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])