import unittest

from job import Job


class PeriodTests(unittest.TestCase):
    def test_get_name(self):
        job = Job('a_name', '', [''], [''], [''])
        self.assertEqual('a_name', job.get_name())

    def test_get_call(self):
        job = Job('', 'a_call', [''], [''], [''])
        self.assertEqual('a_call', job.get_call())

    def test_get_preconditions(self):
        job = Job('', '', ['pre', 'condition'], [''], [''])
        self.assertEqual(['pre', 'condition'], job.get_preconditions())

    def test_get_postconditions(self):
        job = Job('', '', [''], ['post', 'condition'], [''])
        self.assertEqual(['post', 'condition'], job.get_postconditions())

    def test_get_parameters(self):
        job = Job('', '', [''], [''], ['para', 'meter'])
        self.assertEqual(['para', 'meter'], job.get_parameters())
