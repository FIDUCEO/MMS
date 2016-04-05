from workflow import Workflow

w = Workflow('ingest_avhrr_n07', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n07', '1981-09-01', '1985-01-30', 'v01.2')

w.run_ingestion(hosts=[('localhost', 24)], logdir='log')