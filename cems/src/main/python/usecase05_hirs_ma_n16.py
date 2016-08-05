from workflow import Workflow

w = Workflow('usecase05_hirs_ma_n16', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-ma', '2006-11-21', '2014-06-05', '1.0')
w.add_secondary_sensor('hirs-n16', '2006-11-21', '2014-06-05', '1.0')

w.set_usecase_config('usecase-05.xml')

w.run_matchup(hosts=[('localhost', 24)])