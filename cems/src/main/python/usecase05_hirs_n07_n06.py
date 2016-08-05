from workflow import Workflow

w = Workflow('usecase05_hirs_n07_n06', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n07', '1981-08-24', '1983-03-05', '1.0')
w.add_secondary_sensor('hirs-n06', '1981-08-24', '1983-03-05', '1.0')

w.set_usecase_config('usecase-05.xml')

w.run_matchup(hosts=[('localhost', 24)])