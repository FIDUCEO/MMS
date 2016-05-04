from workflow import Workflow

w = Workflow('usecase02_avhrr_m02_n16', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('avhrr-m02', '2006-10-30', '2010-12-31', 'v01.2')
w.add_secondary_sensor('avhrr-n16', '2006-10-30', '2010-12-31', 'v01.2')

w.set_usecase_config('usecase-02.xml')

w.run_matchup(hosts=[('localhost', 24)])