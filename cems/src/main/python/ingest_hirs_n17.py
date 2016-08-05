from workflow import Workflow

w = Workflow('ingest_hirs_n17', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n17', '2002-07-10', '2013-04-09', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])