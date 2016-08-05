from workflow import Workflow

w = Workflow('ingest_hirs_n07', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n07', '1981-08-24', '1984-12-31', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])