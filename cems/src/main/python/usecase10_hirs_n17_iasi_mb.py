from workflow import Workflow

w = Workflow('usecase10_hirs_n17_iasi_mb', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n17', '2013-02-20', '2013-04-09', '1.0')
w.add_secondary_sensor('iasi-mb', '2013-02-20', '2013-04-09', 'latest')

w.set_usecase_config('usecase-10.xml')

w.run_matchup(hosts=[('localhost', 72)])