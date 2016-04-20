from workflow import Workflow

w = Workflow('ingest_avhrr_n15', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n15', '1998-10-26', '2010-12-31', 'v01.2')

w.run_ingestion(hosts=[('localhost', 24)])