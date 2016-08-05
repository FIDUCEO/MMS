from workflow import Workflow

w = Workflow('usecase05_hirs_n08_n07', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n08', '1983-05-03', '1984-12-31', '1.0')
w.add_secondary_sensor('hirs-n07', '1983-05-03', '1984-12-31', '1.0')

w.set_usecase_config('usecase-05.xml')

w.run_matchup(hosts=[('localhost', 24)])