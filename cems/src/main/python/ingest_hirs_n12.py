from workflow import Workflow

w = Workflow('ingest_hirs_n12', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n12', '1991-09-16', '1998-02-12', '1.0')

w.run_ingestion(hosts=[('localhost', 24)])