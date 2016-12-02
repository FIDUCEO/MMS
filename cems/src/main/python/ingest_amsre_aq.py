from workflow import Workflow

w = Workflow('ingest_amsre_aq', 7, '/group_workspaces/cems2/esacci_sst/mms_new/config')
w.add_primary_sensor('amsre-aq', '2002-06-01', '2011-10-04', 'v12')

w.run_ingestion(hosts=[('localhost', 24)])