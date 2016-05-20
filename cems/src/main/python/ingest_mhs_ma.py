from workflow import Workflow

w = Workflow('ingest_mhs_ma', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('mhs-ma', '2006-10-31', '2016-03-07', 'v1.0')

w.run_ingestion(hosts=[('localhost', 24)])