from workflow import Workflow

w = Workflow('usecase10_hirs_n19_iasi_mb', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n19', '2013-02-20', '2016-06-03', '1.0')
w.add_secondary_sensor('iasi-mb', '2013-02-20', '2016-06-03', 'latest')

w.set_usecase_config('usecase-10.xml')

w.run_matchup(hosts=[('localhost', 72)])