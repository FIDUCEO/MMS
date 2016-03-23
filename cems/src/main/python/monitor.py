from pmonitor import PMonitor

class Monitor:
    def __init__(self, preconditions, usecase, hosts, calls, log_dir, simulation):
        """

        :type preconditions: list
        :type usecase: str
        :type hosts: list
        :type calls: list
        :type log_dir: str
        :type simulation: bool
        """
        self.pm = PMonitor(preconditions, usecase, hosts, calls, log_dir=log_dir, simulation=simulation)

    def execute(self, job):
        """

        :type job: Job
        """
        self.pm.execute(job.get_call(), job.get_preconditions(), job.get_postconditions(), job.get_parameters(),
                        log_prefix=job.get_name())

    def wait_for_completion(self):
        self.pm.wait_for_completion()

    def wait_for_completion_and_terminate(self):
        self.pm.wait_for_completion_and_terminate()