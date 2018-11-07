from workflow import Workflow

w = Workflow('ingest_aatsr_en', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('aatsr-en', '2002-05-20', '2012-04-08', 'v3')

w.run_test_job(hosts=[('localhost', 24)])