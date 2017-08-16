from workflow import Workflow

w = Workflow('ingest_ocean_rain_sst', 1, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('ocean-rain-sst', '2010-01-01', '2017-08-31', 'v1.0')

w.run_ingestion(hosts=[('localhost', 1)])