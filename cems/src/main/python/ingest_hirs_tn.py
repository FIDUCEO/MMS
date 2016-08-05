from workflow import Workflow

w = Workflow('ingest_hirs_tn', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-tn', '1978-10-29', '1980-01-30', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])