from workflow import Workflow

w = Workflow('ingest_iasi_ma', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('iasi-ma', '2007-05-29', '2016-11-29', 'latest')

w.run_ingestion(hosts=[('localhost', 24)])