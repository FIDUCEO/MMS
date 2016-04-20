from workflow import Workflow

w = Workflow('ingest_avhrr_n06', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n06', '1979-07-12', '1982-03-17', 'v01.2')

w.run_ingestion(hosts=[('localhost', 24)])