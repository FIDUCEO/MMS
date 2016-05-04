from workflow import Workflow

w = Workflow('usecase02_avhrr_n12_n11', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n12', '1991-09-16', '1994-12-31', 'v01.2')
w.add_secondary_sensor('avhrr-n11', '1991-09-16', '1994-12-31', 'v01.2')

w.set_usecase_config('usecase-02.xml')

w.run_matchup(hosts=[('localhost', 24)])