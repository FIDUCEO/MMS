from workflow import Workflow

w = Workflow('ingest_mhs_mb', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('mhs-mb', '2013-01-15', '2016-03-07', 'v1.0')

w.run_ingestion(hosts=[('localhost', 24)])