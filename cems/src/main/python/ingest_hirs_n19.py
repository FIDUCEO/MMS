from workflow import Workflow

w = Workflow('ingest_hirs_n19', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n19', '2009-04-01', '2016-06-03', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])