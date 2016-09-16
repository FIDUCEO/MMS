from workflow import Workflow

w = Workflow('usecase01_atsr_e1_avhrr_n12', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('atsr-e1', '1991-09-16', '1997-12-17', 'v3')
w.add_secondary_sensor('avhrr-n12', '1991-09-16', '1997-12-17', 'v01.2')

w.set_usecase_config('usecase-01.xml')

w.run_matchup(hosts=[('localhost', 72)])