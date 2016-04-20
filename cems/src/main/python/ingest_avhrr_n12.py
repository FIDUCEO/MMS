from workflow import Workflow

w = Workflow('ingest_avhrr_n12', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n12', '1991-09-16', '1998-12-14', 'v01.2')

w.run_ingestion(hosts=[('localhost', 24)])