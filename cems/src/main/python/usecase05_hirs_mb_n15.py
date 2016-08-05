from workflow import Workflow

w = Workflow('usecase05_hirs_mb_n15', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('hirs-mb', '2013-01-15', '2016-06-03', '1.0')
w.add_secondary_sensor('hirs-n15', '2013-01-15', '2016-06-03', '1.0')

w.set_usecase_config('usecase-05.xml')

w.run_matchup(hosts=[('localhost', 24)])