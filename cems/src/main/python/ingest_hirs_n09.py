from workflow import Workflow

w = Workflow('ingest_hirs_n09', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n09', '1985-02-25', '1988-11-06', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])