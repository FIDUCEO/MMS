from workflow import Workflow

w = Workflow('usecase02_avhrr_n19_n18', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-n19', '2009-02-07', '2015-12-31', 'v01.2')
w.add_secondary_sensor('avhrr-n18', '2009-02-07', '2015-12-31', 'v01.2')

w.set_usecase_config('usecase-02.xml')

w.run_matchup(hosts=[('localhost', 24)])