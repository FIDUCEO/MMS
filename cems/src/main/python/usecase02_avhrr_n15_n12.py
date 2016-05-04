from workflow import Workflow

w = Workflow('usecase02_avhrr_n15_n12', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n15', '1998-10-26', '1998-12-14', 'v01.2')
w.add_secondary_sensor('avhrr-n12', '1998-10-26', '1998-12-14', 'v01.2')

w.set_usecase_config('usecase-02.xml')

w.run_matchup(hosts=[('localhost', 24)])