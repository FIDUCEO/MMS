from period import Period
from workflow import Workflow

period = Period('2002-06-01', '2011-10-07')
w = Workflow('post_process_sst_radiometer', 7, '/group_workspaces/cems2/esacci_sst/mms_new/config', period)

w.set_input_dir('/group_workspaces/cems2/esacci_sst/mms_new/mmd/mmd06c/radiometer-sst_amsre-aq')
w.set_usecase_config('usecase-06-pp.xml')

w.run_post_processing(hosts=[('localhost', 24)])