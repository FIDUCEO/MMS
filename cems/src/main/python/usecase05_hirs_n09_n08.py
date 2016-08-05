from workflow import Workflow

w = Workflow('usecase05_hirs_n09_n08', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n09', '1985-02-25', '1985-10-14', '1.0')
w.add_secondary_sensor('hirs-n08', '1985-02-25', '1985-10-14', '1.0')

w.set_usecase_config('usecase-05.xml')

w.run_matchup(hosts=[('localhost', 24)])