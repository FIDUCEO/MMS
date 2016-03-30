import datetime
import exceptions
import unittest

from period import Period


class PeriodTests(unittest.TestCase):
    def test_period_construction(self):
        period_1 = Period((2007, 1, 1), '2008-01-01')
        self.assertEqual(datetime.date(2007, 1, 1).isoformat(), period_1.get_start_date().isoformat())
        self.assertEqual(datetime.date(2008, 1, 1).isoformat(), period_1.get_end_date().isoformat())

        period_2 = Period('2007-02-01', (2008, 1, 2))
        self.assertEqual(datetime.date(2007, 2, 1).isoformat(), period_2.get_start_date().isoformat())
        self.assertEqual(datetime.date(2008, 1, 2).isoformat(), period_2.get_end_date().isoformat())

        period_3 = Period((2007, 3, 3), datetime.date(2007, 3, 5))
        self.assertEqual(datetime.date(2007, 3, 3).isoformat(), period_3.get_start_date().isoformat())
        self.assertEqual(datetime.date(2007, 3, 5).isoformat(), period_3.get_end_date().isoformat())

        period_4 = Period(datetime.date(1979, 12, 31), (1980, 2, 14))
        self.assertEqual(datetime.date(1979, 12, 31).isoformat(), period_4.get_start_date().isoformat())
        self.assertEqual(datetime.date(1980, 2, 14).isoformat(), period_4.get_end_date().isoformat())

    def test_period_construction_invalid(self):
        try:
            period_1 = Period('2010-01-01', '2009-01-01')
            self.fail()
        except exceptions.ValueError:
            pass

    def test_get_period_intersection(self):
        period_1 = Period('2007-01-01', '2008-01-01')
        period_2 = Period('2007-07-01', '2008-07-01')
        period_3 = Period('2007-10-01', '2007-11-01')
        period_4 = Period('2001-07-01', '2002-07-01')

        self.assertEqual(period_1, period_1.get_intersection(period_1))
        self.assertEqual(period_2, period_2.get_intersection(period_2))

        self.assertEqual(Period('2007-07-01', '2008-01-01'), period_1.get_intersection(period_2))
        self.assertEqual(Period('2007-07-01', '2008-01-01'), period_2.get_intersection(period_1))

        self.assertEqual(period_3, period_1.get_intersection(period_3))

        self.assertTrue(period_1.get_intersection(period_4) is None)

    def test_period_equality(self):
        period_1 = Period((2007, 1, 1), (2008, 1, 1))
        period_2 = Period((2007, 1, 1), (2008, 1, 1))
        self.assertTrue(period_1 == period_2)

        period_3 = Period((2009, 1, 1), (2010, 1, 1))
        self.assertFalse(period_1 == period_3)

    def test_period_inequality(self):
        period_1 = Period((2007, 1, 1), (2008, 1, 1))
        period_2 = Period((2007, 3, 4), (2007, 5, 17))
        self.assertTrue(period_1 != period_2)

        period_3 = Period((2007, 1, 1), (2008, 1, 1))
        self.assertFalse(period_1 != period_3)

    def test_period_greater_than(self):
        period_1 = Period((2007, 1, 1), (2008, 1, 1))
        period_2 = Period((2007, 3, 4), (2007, 5, 17))
        self.assertTrue(period_2 > period_1)
        self.assertFalse(period_1 > period_2)

    def test_period_greater_than_equal(self):
        period_1 = Period((2007, 1, 1), (2008, 1, 1))
        period_2 = Period((2007, 2, 4), (2007, 5, 17))
        period_3 = Period((2007, 1, 1), (2008, 1, 1))
        self.assertTrue(period_2 >= period_1)
        self.assertTrue(period_3 >= period_1)
        self.assertFalse(period_1 >= period_2)

    def test_period_less_than(self):
        period_1 = Period((2007, 1, 1), (2008, 1, 1))
        period_2 = Period((2007, 3, 4), (2007, 5, 17))
        self.assertTrue(period_1 < period_2)
        self.assertFalse(period_2 < period_1)

    def test_period_less_than_equal(self):
        period_1 = Period((2007, 1, 1), (2008, 1, 1))
        period_2 = Period((2007, 3, 4), (2007, 5, 17))
        period_3 = Period((2007, 1, 1), (2008, 1, 1))
        self.assertTrue(period_1 <= period_2)
        self.assertTrue(period_1 <= period_3)
        self.assertFalse(period_2 <= period_1)

    def test_period_is_including(self):
        period_1 = Period((2007, 1, 1), (2008, 1, 1))
        period_2 = Period((2007, 3, 4), (2007, 5, 17))
        self.assertTrue(period_1.is_including(period_2))
        self.assertFalse(period_2.is_including(period_1))

    def test_period_is_intersecting(self):
        period_1 = Period((1999, 7, 14), (2000, 2, 21))
        period_2 = Period((1998, 3, 4), (1999, 10, 2))
        self.assertTrue(period_1.is_intersecting(period_2))
        self.assertTrue(period_2.is_intersecting(period_1))

        period_3 = Period((1999, 7, 14), (2000, 2, 21))
        period_4 = Period((2000, 2, 23), (2001, 1, 12))
        self.assertFalse(period_3.is_intersecting(period_4))
        self.assertFalse(period_4.is_intersecting(period_3))

    def test_period_is_connecting(self):
        period_1 = Period((1999, 7, 14), (2000, 2, 21))
        period_2 = Period((2000, 2, 21), (2003, 10, 2))
        self.assertTrue(period_1.is_connecting(period_2))
        self.assertTrue(period_2.is_connecting(period_1))

        period_3 = Period((1999, 7, 14), (2000, 2, 21))
        period_4 = Period((2000, 2, 23), (2001, 1, 12))
        self.assertFalse(period_3.is_connecting(period_4))
        self.assertFalse(period_4.is_connecting(period_3))

    def test_period_grow_intersecting(self):
        period_1 = Period((1980, 1, 1), (1980, 5, 31))
        period_2 = Period((1980, 4, 1), (1980, 11, 1))

        grown = period_1.grow(period_2)

        self.assertTrue(grown)
        self.assertEqual(Period((1980, 1, 1), (1980, 11, 1)), period_1)

    def test_period_grow_not_intersecting(self):
        period_1 = Period((1980, 1, 1), (1980, 5, 31))
        period_2 = Period((1980, 6, 1), (1980, 11, 1))

        grown = period_1.grow(period_2)

        self.assertFalse(grown)
        self.assertEqual(Period((1980, 1, 1), (1980, 5, 31)), period_1)

    def test_period_grow_inside(self):
        period_1 = Period((1980, 1, 1), (1980, 5, 31))
        period_2 = Period((1980, 2, 1), (1980, 3, 1))

        grown = period_1.grow(period_2)

        self.assertFalse(grown)
        self.assertEqual(Period((1980, 1, 1), (1980, 5, 31)), period_1)

    def test_period_from_isoformat(self):
        # @todo 1 tb/tb find out how we can access this method from a test 2016-03-22
        period_1 = Period((1980, 1, 1), (1980, 5, 31))
