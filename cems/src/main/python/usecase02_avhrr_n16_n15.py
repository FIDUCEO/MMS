from workflow import Workflow

w = Workflow('usecase02_avhrr_n16_n15', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n16', '2001-01-01', '2010-12-31', 'v01.2')
w.add_secondary_sensor('avhrr-n15', '2001-01-01', '2010-12-31', 'v01.2')

w.set_usecase_config('usecase-02.xml')

w.run_matchup(hosts=[('localhost', 24)])