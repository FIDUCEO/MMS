from workflow import Workflow

# Parameters
# 1 - usecase name
# 2 - number of days per time slot
# 3 - configuration directory
w = Workflow('usecase01_aatsr_en_avhrr_m02', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('aatsr-en', '2006-10-30', '2012-04-08', 'v3')
w.add_secondary_sensor('avhrr-m02', '2006-10-30', '2012-04-08', 'v01.2')

w.set_usecase_config('usecase-01.xml')

# Parameters
# 1 - host (usually localhost), number of tasks to submit to scheduler at once
# 2 - number of parallel executed tasks
w.run_matchup(hosts=[('localhost', 12)], num_parallel_tasks=72)