from workflow import Workflow

w = Workflow('usecase05_hirs_n12_n11', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n12', '1991-09-16', '1995-04-10', '1.0')
w.add_secondary_sensor('hirs-n11', '1991-09-16', '1995-04-10', '1.0')

w.set_usecase_config('usecase-05.xml')

w.run_matchup(hosts=[('localhost', 24)])