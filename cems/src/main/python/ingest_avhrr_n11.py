from workflow import Workflow

w = Workflow('ingest_avhrr_n11', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n11', '1988-11-08', '1994-12-31', 'v01.2')

w.run_ingestion(hosts=[('localhost', 24)])