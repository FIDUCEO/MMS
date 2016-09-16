from workflow import Workflow

w = Workflow('ingest_atsr_e1', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('atsr-e1', '1991-08-01', '1997-12-17', 'v3')

w.run_ingestion(hosts=[('localhost', 24)])