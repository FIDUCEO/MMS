from workflow import Workflow

w = Workflow('ingest_mhs_n18', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('mhs-n18', '2005-05-25', '2016-03-04', 'v1.0')

w.run_ingestion(hosts=[('localhost', 24)])