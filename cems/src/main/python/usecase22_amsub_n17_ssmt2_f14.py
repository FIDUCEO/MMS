from workflow import Workflow

w = Workflow('usecase22_amsub_n17_ssmt2_f14', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('amsub-n17', '2002-06-05', '2006-10-30', 'v1.0')
w.add_secondary_sensor('ssmt2-f14', '2002-06-05', '2006-10-30', 'v01')

w.set_usecase_config('usecase-22.xml')

w.run_matchup(hosts=[('localhost', 96)])