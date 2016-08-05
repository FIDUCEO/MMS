from workflow import Workflow

w = Workflow('ingest_hirs_n06', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n06', '1979-06-30', '1983-03-05', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])