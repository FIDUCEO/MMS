import unittest

from workflow import Workflow
from period import Period


class WorkflowTest(unittest.TestCase):
    def test_workflow_get_usecase(self):
        w = Workflow('test')
        self.assertEqual('test', w.get_usecase())

    def test_workflow_get_production_period(self):
        w = Workflow('test', Period((2001,3,24), (2001, 4, 12)))
        self.assertEqual(Period((2001,3,24), (2001, 4, 12)), w.get_production_period())
