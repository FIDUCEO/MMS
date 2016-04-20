from workflow import Workflow

w = Workflow('ingest_avhrr_n10', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n10', '1986-11-17', '1991-09-16', 'v01.2')

w.run_ingestion(hosts=[('localhost', 24)])