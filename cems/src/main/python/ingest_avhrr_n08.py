from workflow import Workflow

w = Workflow('ingest_avhrr_n08', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n08', '1983-05-04', '1985-10-03', 'v01.2')

w.run_ingestion(hosts=[('localhost', 24)])