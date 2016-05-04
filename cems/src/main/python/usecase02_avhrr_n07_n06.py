from workflow import Workflow

w = Workflow('usecase02_avhrr_n07_n06', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n07', '1981-09-01', '1982-03-17', 'v01.2')
w.add_secondary_sensor('avhrr-n06', '1981-09-01', '1982-03-17', 'v01.2')

w.set_usecase_config('usecase-02.xml')

w.run_matchup(hosts=[('localhost', 24)])