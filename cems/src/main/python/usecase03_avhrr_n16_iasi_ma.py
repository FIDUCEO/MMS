from workflow import Workflow

w = Workflow('usecase02_avhrr_n16_iasi_ma', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n16', '2007-05-29', '2010-12-31', 'v01.2')
w.add_secondary_sensor('iasi-ma', '2007-05-29', '2010-12-31', 'latest')

w.set_usecase_config('usecase-03.xml')

w.run_matchup(hosts=[('localhost', 72)])