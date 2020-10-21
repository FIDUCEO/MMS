from workflow import Workflow

# Parameters
# 1 - usecase name
# 2 - number of days per time slot
# 3 - configuration directory
w = Workflow('ingest_aatsr_en', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('aatsr-en', '2002-05-20', '2012-04-08', 'v3')

# Parameters
# 1 - host (usually localhost), number of tasks to submit to scheduler at once
# 2 - number of parallel executed tasks
w.run_ingestion(hosts=[('localhost', 10)], num_parallel_tasks=24)