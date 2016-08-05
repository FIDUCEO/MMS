from workflow import Workflow

w = Workflow('usecase05_hirs_n15_n14', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n15', '1999-01-01', '1999-12-31', '1.0')
w.add_secondary_sensor('hirs-n14', '1999-01-01', '1999-12-31', '1.0')

w.set_usecase_config('usecase-05.xml')

w.run_matchup(hosts=[('localhost', 24)])