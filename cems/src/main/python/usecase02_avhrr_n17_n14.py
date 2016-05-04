from workflow import Workflow

w = Workflow('usecase02_avhrr_n17_n14', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n17', '2002-06-25', '2010-10-07', 'v01.2')
w.add_secondary_sensor('avhrr-n14', '2002-06-25', '2010-10-07', 'v01.2')

w.set_usecase_config('usecase-02.xml')

w.run_matchup(hosts=[('localhost', 24)])