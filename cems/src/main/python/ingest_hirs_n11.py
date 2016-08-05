from workflow import Workflow

w = Workflow('ingest_hirs_n11', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n11', '1988-11-08', '1995-04-10', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])