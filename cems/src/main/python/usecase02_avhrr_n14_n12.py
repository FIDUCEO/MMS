from workflow import Workflow

w = Workflow('usecase02_avhrr_n14_n12', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n14', '1995-01-01', '1998-12-14', 'v01.2')
w.add_secondary_sensor('avhrr-n12', '1995-01-01', '1998-12-14', 'v01.2')

w.set_usecase_config('usecase-02.xml')

w.run_matchup(hosts=[('localhost', 24)])