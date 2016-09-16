from workflow import Workflow

w = Workflow('usecase01_atsr_e2_avhrr_n15', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('atsr-e2', '1998-10-26', '2008-01-31', 'v3')
w.add_secondary_sensor('avhrr-n15', '1998-10-26', '2008-01-31', 'v01.2')

w.set_usecase_config('usecase-01.xml')

w.run_matchup(hosts=[('localhost', 72)])