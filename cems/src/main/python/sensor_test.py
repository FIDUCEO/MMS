import unittest

from period import Period
from sensor import Sensor


class SensorTest(unittest.TestCase):
    def test_get_sensor_name(self):
        sensor = Sensor('atsr-en', Period((2007, 1, 1), (2008, 1, 1)))
        self.assertEqual('atsr-en', sensor.get_name())

    def test_get_sensor_period(self):
        sensor = Sensor('atsr-e1', Period((2007, 1, 1), (2008, 1, 1)))
        self.assertEqual('2007-01-01', sensor.get_period().get_start_date().isoformat())
        self.assertEqual('2008-01-01', sensor.get_period().get_end_date().isoformat())

    def test_get_version_default(self):
        sensor = Sensor('atsr-en', Period((2007, 1, 1), (2008, 1, 1)))
        self.assertEqual('', sensor.get_version())

    def test_get_version(self):
        sensor = Sensor('atsr-en', Period((2007, 1, 1), (2008, 1, 1)), 'a_version')
        self.assertEqual('a_version', sensor.get_version())

    def test_sensor_equality(self):
        sensor_1 = Sensor('atsr-e2', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('atsr-e2', Period((2007, 1, 1), (2008, 1, 1)))
        self.assertTrue(sensor_1 == sensor_2)

    def test_sensor_inequality(self):
        sensor_1 = Sensor('avhrr-n08', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('avhrr-n09', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_3 = Sensor('avhrr-n10', Period((2008, 1, 1), (2009, 1, 1)))
        self.assertTrue(sensor_1 != sensor_2)
        self.assertTrue(sensor_1 != sensor_3)

    def test_sensor_ge(self):
        sensor_1 = Sensor('atsr-en', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('atsr-e2', Period((2008, 1, 1), (2009, 1, 1)))
        self.assertTrue(sensor_1 >= sensor_2)

        sensor_3 = Sensor('atsr-e2', Period((2009, 1, 1), (2010, 1, 1)))
        self.assertTrue(sensor_2 >= sensor_3)
        self.assertTrue(sensor_3 >= sensor_2)
