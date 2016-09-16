from workflow import Workflow

w = Workflow('usecase01_aatsr_en_avhrr_n19', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('aatsr-en', '2009-02-07', '2012-04-08', 'v3')
w.add_secondary_sensor('avhrr-n19', '2009-02-07', '2012-04-08', 'v01.2')

w.set_usecase_config('usecase-01.xml')

w.run_matchup(hosts=[('localhost', 72)])