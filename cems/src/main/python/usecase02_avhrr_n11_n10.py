from workflow import Workflow

w = Workflow('usecase02_avhrr_n11_n10', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n11', '1988-11-08', '1991-09-16', 'v01.2')
w.add_secondary_sensor('avhrr-n10', '1988-11-08', '1991-09-16', 'v01.2')

w.set_usecase_config('usecase-02.xml')

w.run_matchup(hosts=[('localhost', 24)])