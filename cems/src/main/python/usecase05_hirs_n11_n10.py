from workflow import Workflow

w = Workflow('usecase05_hirs_n11_n10', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n11', '1988-11-08', '1991-09-16', '1.0')
w.add_secondary_sensor('hirs-n10', '1988-11-08', '1991-09-16', '1.0')

w.set_usecase_config('usecase-05.xml')

w.run_matchup(hosts=[('localhost', 24)])