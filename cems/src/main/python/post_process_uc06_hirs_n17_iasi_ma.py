from period import Period
from workflow import Workflow

period = Period('2007-05-29', '2013-04-09')
w = Workflow('post_process_uc06_hirs_n17_iasi_ma', 7, '/group_workspaces/cems2/esacci_sst/mms_new/config', period)

w.set_input_dir('/group_workspaces/cems2/fiduceo/Data/mms/mmd/mmd06/hirs_n17_iasi_ma')
w.set_usecase_config('usecase-06-pp.xml')

w.run_post_processing(hosts=[('localhost', 48)])