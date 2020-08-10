import os
import unittest

from jasmin.jasmin_job_monitor import JasminJobMonitor, LSFInterface, SLURMInterface


class JasminJobMonitorTest(unittest.TestCase):

    def test_create_scheduler_interface_no_environ(self):
        try:
            JasminJobMonitor._create_scheduler_interface()
            self.fail("ValueError expected")
        except(ValueError):
            pass

    def test_create_scheduler_interface_LSF(self):
        try:
            os.environ["SCHEDULER"] = "LSF"

            scheduler_interface = JasminJobMonitor._create_scheduler_interface()
            self.assertIsInstance(scheduler_interface, LSFInterface)
        finally:
            del os.environ["SCHEDULER"]

    def test_create_scheduler_interface_SLURM(self):
        try:
            os.environ["SCHEDULER"] = "SLURM"

            scheduler_interface = JasminJobMonitor._create_scheduler_interface()
            self.assertIsInstance(scheduler_interface, SLURMInterface)
        finally:
            del os.environ["SCHEDULER"]

    def test_create_scheduler_interface_nonsense(self):
        try:
            os.environ["SCHEDULER"] = "heffalump"

            JasminJobMonitor._create_scheduler_interface()
            self.fail("ValueError expected")
        except(ValueError):
            pass
        finally:
            del os.environ["SCHEDULER"]