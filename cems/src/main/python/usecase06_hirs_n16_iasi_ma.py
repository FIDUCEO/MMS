from workflow import Workflow

w = Workflow('usecase06_hirs_n16_iasi_ma', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n16', '2007-05-29', '2014-06-05', '1.0')
w.add_secondary_sensor('iasi-ma', '2007-05-29', '2014-06-05', 'latest')

w.set_usecase_config('usecase-06.xml')

w.run_matchup(hosts=[('localhost', 72)])