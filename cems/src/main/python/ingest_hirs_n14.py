from workflow import Workflow

w = Workflow('ingest_hirs_n14', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n14', '1995-01-01', '1999-12-31', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])