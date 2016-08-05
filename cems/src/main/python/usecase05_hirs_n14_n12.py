from workflow import Workflow

w = Workflow('usecase05_hirs_n14_n12', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n14', '1995-01-01', '1998-02-12', '1.0')
w.add_secondary_sensor('hirs-n12', '1995-01-01', '1998-02-12', '1.0')

w.set_usecase_config('usecase-05.xml')

w.run_matchup(hosts=[('localhost', 24)])