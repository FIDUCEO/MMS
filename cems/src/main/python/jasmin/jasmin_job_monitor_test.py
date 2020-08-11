import os
import unittest

from jasmin.jasmin_job_monitor import JasminJobMonitor, LSFInterface, SLURMInterface
from jasmin.status_codes import StatusCodes


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
            os.environ["MMS_USER"] = "HarryPotter"

            scheduler_interface = JasminJobMonitor._create_scheduler_interface()
            self.assertIsInstance(scheduler_interface, SLURMInterface)
        finally:
            del os.environ["SCHEDULER"]
            del os.environ["MMS_USER"]

    def test_create_scheduler_interface_nonsense(self):
        try:
            os.environ["SCHEDULER"] = "heffalump"

            JasminJobMonitor._create_scheduler_interface()
            self.fail("ValueError expected")
        except(ValueError):
            pass
        finally:
            del os.environ["SCHEDULER"]

    def test_parse_job_status_SLURM(self):
        output = "             JOBID PARTITION     NAME     USER ST       TIME  NODES NODELIST(REASON)\n" + "8158926 short-ser the_job. tblock01  R       1:46      1 host143\n"

        try:
            os.environ["MMS_USER"] = "HarryPotter"

            scheduler = SLURMInterface()
            job_status_dict = scheduler.parse_jobs_call(output)
            self.assertEqual(1, len(job_status_dict))
            self.assertEqual(StatusCodes.RUNNING, job_status_dict["8158926"])
        finally:
            del os.environ["MMS_USER"]

    def test_status_to_enum_LSF(self):
        scheduler = LSFInterface()

        self.assertEqual(StatusCodes.RUNNING, scheduler._status_to_enum("RUN"))

        self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("PEND"))
        self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("PROV"))
        self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("WAIT"))

        self.assertEqual(StatusCodes.FAILED, scheduler._status_to_enum("EXIT"))

        self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("PSUSP"))
        self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("USUSP"))
        self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("SSUSP"))
        self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("ZOMBI"))

        self.assertEqual(StatusCodes.DONE, scheduler._status_to_enum("DONE"))

        self.assertEqual(StatusCodes.UNKNOWN, scheduler._status_to_enum("UNKWN"))

    def test_status_to_enum_SLURM(self):
        try:
            os.environ["MMS_USER"] = "RonWeasley"

            scheduler = SLURMInterface()
            self.assertEqual(StatusCodes.RUNNING, scheduler._status_to_enum("R"))
            self.assertEqual(StatusCodes.RUNNING, scheduler._status_to_enum("CG"))
            self.assertEqual(StatusCodes.RUNNING, scheduler._status_to_enum("SO"))

            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("CF"))
            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("PD"))
            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("RD"))
            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("RF"))
            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("RH"))
            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("RQ"))
            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("RS"))
            self.assertEqual(StatusCodes.SCHEDULED, scheduler._status_to_enum("RV"))

            self.assertEqual(StatusCodes.FAILED, scheduler._status_to_enum("BF"))
            self.assertEqual(StatusCodes.FAILED, scheduler._status_to_enum("DL"))
            self.assertEqual(StatusCodes.FAILED, scheduler._status_to_enum("F"))
            self.assertEqual(StatusCodes.FAILED, scheduler._status_to_enum("NF"))
            self.assertEqual(StatusCodes.FAILED, scheduler._status_to_enum("OOM"))
            self.assertEqual(StatusCodes.FAILED, scheduler._status_to_enum("TO"))

            self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("CA"))
            self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("PR"))
            self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("SI"))
            self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("ST"))
            self.assertEqual(StatusCodes.DROPPED, scheduler._status_to_enum("S"))

            self.assertEqual(StatusCodes.DONE, scheduler._status_to_enum("CD"))
            self.assertEqual(StatusCodes.DONE, scheduler._status_to_enum("SE"))
        finally:
            del os.environ["MMS_USER"]
