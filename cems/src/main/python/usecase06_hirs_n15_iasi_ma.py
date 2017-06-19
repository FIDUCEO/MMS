from workflow import Workflow

w = Workflow('usecase06_hirs_n15_iasi_ma', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n15', '2007-05-29', '2016-06-03', '1.0')
w.add_secondary_sensor('iasi-ma', '2007-05-29', '2016-06-03', 'latest')

w.set_usecase_config('usecase-06.xml')

w.run_matchup(hosts=[('localhost', 72)])