from workflow import Workflow

w = Workflow('usecase22_amsub_n16_ssmt2_f15', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('amsub-n16', '2000-09-21', '2008-05-28', 'v1.0')
w.add_secondary_sensor('ssmt2-f15', '2000-09-21', '2008-05-28', 'v01')

w.set_usecase_config('usecase-22.xml')

w.run_matchup(hosts=[('localhost', 96)])