from workflow import Workflow

w = Workflow('usecase17_amsub_n16_amsub_n15', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('amsub-n16', '2000-09-21', '2014-06-05', 'v1.0')
w.add_secondary_sensor('amsub-n15', '2000-09-21', '2014-06-05', 'v1.0')

w.set_usecase_config('usecase-17.xml')

w.run_matchup(hosts=[('localhost', 72)])