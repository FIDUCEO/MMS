from workflow import Workflow

w = Workflow('usecase10_hirs_n16_iasi_mb', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n16', '2013-02-20', '2014-06-05', '1.0')
w.add_secondary_sensor('iasi-mb', '2013-02-20', '2014-06-05', 'latest')

w.set_usecase_config('usecase-10.xml')

w.run_matchup(hosts=[('localhost', 72)])