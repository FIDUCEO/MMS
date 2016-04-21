from workflow import Workflow

w = Workflow('ingest_avhrr_m02', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-m02', '2006-10-30', '2015-12-31', 'v01.2')

w.run_ingestion(hosts=[('localhost', 24)])