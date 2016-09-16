from workflow import Workflow

w = Workflow('ingest_atsr_e2', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('atsr-e2', '1995-06-01', '2008-01-31', 'v3')

w.run_ingestion(hosts=[('localhost', 24)])