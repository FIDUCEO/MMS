from workflow import Workflow

w = Workflow('ingest_avhrr_n16', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n16', '2001-01-01', '2010-12-31', 'v01.2')

w.run_ingestion(hosts=[('localhost', 24)])