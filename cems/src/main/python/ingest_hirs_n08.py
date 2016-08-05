from workflow import Workflow

w = Workflow('ingest_hirs_n08', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n08', '1983-05-03', '1985-10-14', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])