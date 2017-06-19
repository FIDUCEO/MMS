from workflow import Workflow

w = Workflow('usecase06_hirs_n19_iasi_ma', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n19', '2009-04-01', '2016-06-03', '1.0')
w.add_secondary_sensor('iasi-ma', '2009-04-01', '2016-06-03', 'latest')

w.set_usecase_config('usecase-06.xml')

w.run_matchup(hosts=[('localhost', 72)])