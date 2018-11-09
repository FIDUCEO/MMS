import unittest

from period import Period
from sensor import Sensor
from sensorpair import SensorPair


class SensorPairTest(unittest.TestCase):
    def test_sensor_pair_construction(self):
        sensor_1 = Sensor('avhrr-n12', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('avhrr-n13', Period((2007, 7, 1), (2008, 7, 1)))
        sensor_3 = Sensor('avhrr-n14', Period((2008, 1, 1), (2009, 1, 1)))

        sensor_pair = SensorPair(sensor_1, sensor_2)
        self.assertEqual('avhrr-n12', sensor_pair.get_primary_name())
        self.assertEqual('avhrr-n13', sensor_pair.get_secondary_name())
        self.assertEqual(Period((2007, 7, 1), (2008, 1, 1)), sensor_pair.get_period())

        sensor_pair = SensorPair(sensor_3, sensor_2)
        self.assertEqual('avhrr-n14', sensor_pair.get_primary_name())
        self.assertEqual('avhrr-n13', sensor_pair.get_secondary_name())
        self.assertEqual(Period((2008, 1, 1), (2008, 7, 1)), sensor_pair.get_period())

    def test_sensor_pair_construction_invalid(self):
        sensor_1 = Sensor('avhrr-n14', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('avhrr-n15', Period((2008, 1, 1), (2009, 1, 1)))

        try:
            SensorPair(sensor_1, sensor_2)
            self.fail("ValueError expected")
        except ValueError:
            pass

        try:
            SensorPair(sensor_2, sensor_1)
            self.fail("ValueError expected")
        except ValueError:
            pass

    def test_sensor_pair_construction_with_production_period(self):
        sensor_1 = Sensor('avhrr-n12', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('avhrr-n13', Period((2007, 7, 1), (2008, 7, 1)))

        sensor_pair = SensorPair(sensor_1, sensor_2, Period((2007,8,1), (2007, 9, 1)))
        self.assertEqual(Period((2007,8,1), (2007, 9, 1)), sensor_pair.get_period())

    def test_sensor_pair_construction_with_production_period_invalid(self):
        sensor_1 = Sensor('avhrr-n12', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('avhrr-n13', Period((2007, 7, 1), (2008, 7, 1)))

        try:
            SensorPair(sensor_1, sensor_2, Period((2000,8,1), (2000, 9, 1)))
            self.fail("ValueError exoected")
        except ValueError:
            pass


    def test_sensor_pair_get_name_different_sensor_names(self):
        sensor_1 = Sensor('atsr-en', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('atsr-e2', Period((2007, 7, 1), (2008, 7, 1)))

        sensor_pair = SensorPair(sensor_1, sensor_2)
        self.assertEqual('atsr-en,atsr-e2', sensor_pair.get_name())

    def test_sensor_pair_get_name_same_sensor_names(self):
        sensor_1 = Sensor('avhrr-m01', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('avhrr-m01', Period((2007, 7, 1), (2008, 7, 1)))

        sensor_pair = SensorPair(sensor_1, sensor_2)
        self.assertEqual('avhrr-m01', sensor_pair.get_name())

    def test_sensor_pair_get_primary_name(self):
        sensor_1 = Sensor('avhrr-m01', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('avhrr-m02', Period((2007, 7, 1), (2008, 7, 1)))

        sensor_pair = SensorPair(sensor_1, sensor_2)
        self.assertEqual('avhrr-m01', sensor_pair.get_primary_name())

    def test_sensor_pair_get_secondary_name(self):
        sensor_1 = Sensor('avhrr-m01', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('avhrr-m02', Period((2007, 7, 1), (2008, 7, 1)))

        sensor_pair = SensorPair(sensor_1, sensor_2)
        self.assertEqual('avhrr-m02', sensor_pair.get_secondary_name())

    def test_sensor_pair_get_primary(self):
        sensor_1 = Sensor('avhrr-m01', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('avhrr-m02', Period((2007, 7, 1), (2008, 7, 1)))

        sensor_pair = SensorPair(sensor_1, sensor_2)
        primary = sensor_pair.get_primary()
        self.assertEqual('avhrr-m01', primary.get_name())

    def test_sensor_pair_get_secondary(self):
        sensor_1 = Sensor('avhrr-m01', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('avhrr-m02', Period((2007, 7, 1), (2008, 7, 1)))

        sensor_pair = SensorPair(sensor_1, sensor_2)
        secondary = sensor_pair.get_secondary()
        self.assertEqual('avhrr-m02', secondary.get_name())

    def test_sensor_pair_equality(self):
        sensor_1 = Sensor('atsr-en', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('atsr-e2', Period((2007, 7, 1), (2008, 7, 1)))

        sensor_pair_1 = SensorPair(sensor_1, sensor_2)
        sensor_pair_2 = SensorPair(sensor_2, sensor_1)
        self.assertTrue(sensor_pair_1 == sensor_pair_2)

    def test_sensor_pair_inequality(self):
        sensor_1 = Sensor('atsr-en', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('atsr-e2', Period((2007, 7, 1), (2008, 7, 1)))
        sensor_3 = Sensor('atsr-e1', Period((2008, 1, 1), (2009, 1, 1)))

        sensor_pair_1 = SensorPair(sensor_1, sensor_2)
        sensor_pair_2 = SensorPair(sensor_2, sensor_3)
        self.assertTrue(sensor_pair_1 != sensor_pair_2)
        self.assertTrue(sensor_pair_2 != sensor_pair_1)

    def test_sensor_pair_ge(self):
        sensor_1 = Sensor('atsr-en', Period((2007, 1, 1), (2008, 1, 1)))
        sensor_2 = Sensor('atsr-e2', Period((2007, 7, 1), (2008, 7, 1)))
        sensor_3 = Sensor('atsr-e1', Period((2007, 10, 1), (2008, 10, 1)))

        sensor_pair_1 = SensorPair(sensor_1, sensor_2)
        sensor_pair_2 = SensorPair(sensor_2, sensor_3)
        sensor_pair_3 = SensorPair(sensor_1, sensor_3)
        self.assertTrue(sensor_pair_1 >= sensor_pair_2)
        self.assertTrue(sensor_pair_1 >= sensor_pair_3)
