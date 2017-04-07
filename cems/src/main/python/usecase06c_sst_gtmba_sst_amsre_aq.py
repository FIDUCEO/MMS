from workflow import Workflow

w = Workflow('usecase06c_sst_gtmba_sst_amsre_aq', 7, '/group_workspaces/cems2/esacci_sst/mms_new/config')
w.add_primary_sensor('gtmba-sst', '2002-06-01', '2011-10-04', 'v03.3')
w.add_secondary_sensor('amsre-aq', '2002-06-01', '2011-10-04', 'v12')

w.set_usecase_config('usecase-06-sst.xml')

w.run_matchup(hosts=[('localhost', 24)])