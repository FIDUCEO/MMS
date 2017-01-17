from period import Period
from workflow import Workflow

period = Period('2004-01-08', '2011-10-07')
w = Workflow('post_process_sst_animal', 7, '/group_workspaces/cems2/esacci_sst/mms_new/config')

w.set_input_dir('/group_workspaces/cems2/esacci_sst/mms_new/mmd/mmd06c/animal-sst_amsre-aq')
w.set_usecase_config('usecase-06-pp.xml')

w.run_post_processing(hosts=[('localhost', 24)])