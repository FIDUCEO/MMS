from period import Period
from workflow import Workflow

# Parameters
# 1 - usecase name
# 2 - number of days per time slot
# 3 - configuration directory
# 4 - processing period
period = Period('2004-01-08', '2011-10-07')
w = Workflow('post_process_sst_animal', 7, '/group_workspaces/cems2/esacci_sst/mms_new/config', period)

w.set_input_dir('/group_workspaces/cems2/esacci_sst/mms_new/mmd/mmd06c/animal-sst_amsre-aq')
w.set_usecase_config('usecase-06-pp.xml')

# Parameters
# 1 - host (usually localhost), number of tasks to submit to scheduler at once
# 2 - number of parallel executed tasks
w.run_post_processing(hosts=[('localhost', 10)], num_parallel_tasks=24)