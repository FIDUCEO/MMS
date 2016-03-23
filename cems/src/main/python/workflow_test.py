import unittest

from period import Period
from sensor import Sensor
from workflow import Workflow


class WorkflowTest(unittest.TestCase):
    def test_get_usecase(self):
        w = Workflow('test', 1)
        self.assertEqual('test', w.get_usecase())

    def test_get_production_period(self):
        w = Workflow('test', 2, Period((2001, 3, 24), (2001, 4, 12)))
        self.assertEqual(Period((2001, 3, 24), (2001, 4, 12)), w.get_production_period())

    def test_get_samples_per_time_slot_default(self):
        w = Workflow('test', 3)
        self.assertEqual(50000, w.get_samples_per_time_slot())

    def test_set_get_samples_per_time_slot_default(self):
        w = Workflow('test', 4)
        w.set_samples_per_time_slot(816)
        self.assertEqual(816, w.get_samples_per_time_slot())

    def test_get_samples_time_slot_days(self):
        w = Workflow('test', 5)
        self.assertEqual(5, w.get_time_slot_days())

    def test_get_primary_sensors_empty_list(self):
        w = Workflow('test', 5)
        self.assertEqual([], w._get_primary_sensors())

    def test_add_get_primary_sensors(self):
        w = Workflow('test', 5)
        w.add_primary_sensor('atsr-e2', '1995-06-01', '1996-01-01')
        self.assertEqual([Sensor('atsr-e2', Period((1995, 6, 1), (1996, 1, 1)))], w._get_primary_sensors())

    def test_add_get_primary_sensors_multiple(self):
        w = Workflow('test', 5)
        w.add_primary_sensor('atsr-e2', '1995-06-01', '1996-01-01')
        w.add_primary_sensor('atsr-e1', '1991-06-01', '1995-01-01')
        w.add_primary_sensor('atsr-en', '1996-01-01', '1998-01-01')

        sensors = w._get_primary_sensors()
        self.assertEqual([Sensor('atsr-en', Period((1996, 1, 1), (1998, 1, 1))),
                          Sensor('atsr-e2', Period((1995, 6, 1), (1996, 1, 1))),
                          Sensor('atsr-e1', Period((1991, 6, 1), (1995, 1, 1)))], sensors)

    def test_get_secondary_sensors_empty_list(self):
        w = Workflow('test', 7)
        self.assertEqual([], w._get_secondary_sensors())

    def test_add_get_secondary_sensors(self):
        w = Workflow('test', 8)
        w.add_secondary_sensor('avhrr-n16', '1995-06-01', '1996-01-01')
        self.assertEqual([Sensor('avhrr-n16', Period((1995, 6, 1), (1996, 1, 1)))], w._get_secondary_sensors())

    def test_add_get_secondary_sensors_multiple(self):
        w = Workflow('test', 9)
        w.add_secondary_sensor('atsr-e2', '1996-06-01', '1997-01-01')
        w.add_secondary_sensor('atsr-e1', '1992-06-01', '1996-01-01')
        w.add_secondary_sensor('atsr-en', '1997-01-01', '1999-01-01')

        sensors = w._get_secondary_sensors()
        self.assertEqual([Sensor('atsr-en', Period((1997, 1, 1), (1999, 1, 1))),
                          Sensor('atsr-e2', Period((1996, 6, 1), (1997, 1, 1))),
                          Sensor('atsr-e1', Period((1992, 6, 1), (1996, 1, 1)))], sensors)