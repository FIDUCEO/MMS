from workflow import Workflow

w = Workflow('ingest_xbt_sst', 7, '/group_workspaces/cems2/esacci_sst/mms_new/config')
w.add_primary_sensor('xbt-sst', '1978-01-01', '2016-12-31', 'v03.3')

w.run_ingestion(hosts=[('localhost', 1)])