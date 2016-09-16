from workflow import Workflow

w = Workflow('usecase01_atsr_e1_avhrr_n11', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('atsr-e1', '1991-08-01', '1994-12-31', 'v3')
w.add_secondary_sensor('avhrr-n11', '1991-08-01', '1994-12-31', 'v01.2')

w.set_usecase_config('usecase-01.xml')

w.run_matchup(hosts=[('localhost', 72)])