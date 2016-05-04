from workflow import Workflow

w = Workflow('usecase02_avhrr_n10_n09', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n10', '1986-11-17', '1988-11-07', 'v01.2')
w.add_secondary_sensor('avhrr-n09', '1986-11-17', '1988-11-07', 'v01.2')

w.set_usecase_config('usecase-02.xml')

w.run_matchup(hosts=[('localhost', 24)])