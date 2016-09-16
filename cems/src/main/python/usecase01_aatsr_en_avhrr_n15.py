from workflow import Workflow

w = Workflow('usecase01_aatsr_en_avhrr_n15', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('aatsr-en', '2002-05-20', '2010-12-31', 'v3')
w.add_secondary_sensor('avhrr-n15', '2002-05-20', '2010-12-31', 'v01.2')

w.set_usecase_config('usecase-01.xml')

w.run_matchup(hosts=[('localhost', 72)])