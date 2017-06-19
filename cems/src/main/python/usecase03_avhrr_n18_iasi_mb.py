from workflow import Workflow

w = Workflow('usecase02_avhrr_n18_iasi_mb', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n18', '2013-02-20', '2015-12-31', 'v01.2')
w.add_secondary_sensor('iasi-mb', '2013-02-20', '2015-12-31', 'latest')

w.set_usecase_config('usecase-03.xml')

w.run_matchup(hosts=[('localhost', 72)])