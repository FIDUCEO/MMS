from workflow import Workflow

w = Workflow('ingest_iasi_mb', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('iasi-mb', '2013-02-20', '2016-11-29', 'latest')

w.run_ingestion(hosts=[('localhost', 24)])