from workflow import Workflow

w = Workflow('ingest_avhrr_n18', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n18', '2005-05-20', '2015-12-31', 'v01.2')

w.run_ingestion(hosts=[('localhost', 24)])