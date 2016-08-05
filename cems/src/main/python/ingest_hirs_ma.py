from workflow import Workflow

w = Workflow('ingest_hirs_ma', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-ma', '2006-11-21', '2016-06-03', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])