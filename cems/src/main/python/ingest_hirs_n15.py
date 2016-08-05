from workflow import Workflow

w = Workflow('ingest_hirs_n15', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n15', '1999-01-01', '2016-06-03', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])