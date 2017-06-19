from period import Period
from workflow import Workflow

period = Period('2013-02-20', '2014-06-05')
w = Workflow('post_process_uc10_hirs_n16_iasi_mb', 7, '/group_workspaces/cems2/esacci_sst/mms_new/config', period)

w.set_input_dir('/group_workspaces/cems2/fiduceo/Data/mms/mmd/mmd10/hirs_n16_iasi_mb')
w.set_usecase_config('usecase-10-pp.xml')

w.run_post_processing(hosts=[('localhost', 48)])