from workflow import Workflow

w = Workflow('usecase05_hirs_n10_n09', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n10', '1986-11-30', '1988-11-06', '1.0')
w.add_secondary_sensor('hirs-n09', '1986-11-30', '1988-11-06', '1.0')

w.set_usecase_config('usecase-05.xml')

w.run_matchup(hosts=[('localhost', 24)])