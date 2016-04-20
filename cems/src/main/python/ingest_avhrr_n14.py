from workflow import Workflow

w = Workflow('ingest_avhrr_n14', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n14', '1995-01-01', '2002-10-07', 'v01.2')

w.run_ingestion(hosts=[('localhost', 24)])