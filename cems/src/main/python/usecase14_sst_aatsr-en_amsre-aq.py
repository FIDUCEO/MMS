from workflow import Workflow

w = Workflow('usecase14_sst_aatsr_en_amsre_aq', 7, '/group_workspaces/cems2/esacci_sst/mms_new/config')
w.add_primary_sensor('amsre-aq', '2002-06-01', '2011-10-04', 'v12')
w.add_secondary_sensor('aatsr-en', '2002-06-01', '2011-10-04', 'v3')

w.set_usecase_config('usecase-14-sst.xml')

w.run_matchup(hosts=[('localhost', 72)])