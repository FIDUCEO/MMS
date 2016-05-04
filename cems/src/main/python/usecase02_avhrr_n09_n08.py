from workflow import Workflow

w = Workflow('usecase02_avhrr_n09_n08', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n09', '1985-02-27', '1985-10-03', 'v01.2')
w.add_secondary_sensor('avhrr-n08', '1985-02-27', '1985-10-03', 'v01.2')

w.set_usecase_config('usecase-02.xml')

w.run_matchup(hosts=[('localhost', 24)])