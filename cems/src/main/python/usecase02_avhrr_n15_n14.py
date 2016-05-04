from workflow import Workflow

w = Workflow('usecase02_avhrr_n15_n14', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n15', '1998-10-26', '2002-10-07', 'v01.2')
w.add_secondary_sensor('avhrr-n14', '1998-10-26', '2002-10-07', 'v01.2')

w.set_usecase_config('usecase-02.xml')

w.run_matchup(hosts=[('localhost', 24)])