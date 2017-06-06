from workflow import Workflow

w = Workflow('usecase03_sst_drifter_sst_aatsr_en', 7, '/group_workspaces/cems2/esacci_sst/mms_new/config')
w.add_primary_sensor('drifter-sst', '2008-06-01', '2008-06-30', 'v03.3')
w.add_secondary_sensor('aatsr-en', '2008-06-01', '2008-06-30', 'v3')

w.set_usecase_config('usecase-03-sst.xml')

w.run_matchup(hosts=[('localhost', 6)])