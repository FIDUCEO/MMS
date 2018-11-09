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
        self.pm = PMonitor(preconditions, usecase, hosts, calls, logdir=log_dir, simulation=simulation, polling="job_status_callback.sh")

    def execute(self, job):
        """

        :type job: Job
        """
        self.pm.execute(job.get_call(), job.get_preconditions(), job.get_postconditions(), job.get_parameters(),
                        logprefix=job.get_name())

    def wait_for_completion(self):
        self.pm.wait_for_completion()