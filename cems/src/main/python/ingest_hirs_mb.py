from workflow import Workflow

w = Workflow('ingest_hirs_mb', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-mb', '2013-01-15', '2016-06-03', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])