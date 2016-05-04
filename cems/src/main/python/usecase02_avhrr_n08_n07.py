from workflow import Workflow

w = Workflow('usecase02_avhrr_n08_n07', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n08', '1983-05-04', '1985-01-30', 'v01.2')
w.add_secondary_sensor('avhrr-n07', '1983-05-04', '1985-01-30', 'v01.2')

w.set_usecase_config('usecase-02.xml')

w.run_matchup(hosts=[('localhost', 24)])