from workflow import Workflow

w = Workflow('ingest_avhrr_n09', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n09', '1985-02-27', '1988-11-07', 'v01.2')

w.run_ingestion(hosts=[('localhost', 24)])