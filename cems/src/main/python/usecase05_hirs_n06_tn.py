from workflow import Workflow

w = Workflow('usecase05_hirs_n06_tn', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-n06', '1979-06-30', '1980-01-30', '1.0')
w.add_secondary_sensor('hirs-tn', '1979-06-30', '1980-01-30', '1.0')

w.set_usecase_config('usecase-05.xml')

w.run_matchup(hosts=[('localhost', 24)])