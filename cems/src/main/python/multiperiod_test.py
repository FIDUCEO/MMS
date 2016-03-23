import unittest

from multiperiod import MultiPeriod
from period import Period


class MultiPeriodTests(unittest.TestCase):

    def test_multi_period(self):
        multi_period = MultiPeriod()
        period_1 = Period('2007-01-01', '2007-02-01')
        period_2 = Period('2007-02-01', '2007-03-01')
        period_3 = Period('2006-12-01', '2007-01-01')
        period_4 = Period('2006-11-01', '2007-01-01')
        period_5 = Period('2007-02-01', '2007-04-01')
        period_6 = Period('2007-05-01', '2007-06-01')
        period_7 = Period('2007-04-01', '2007-05-01')
        period_8 = Period('2007-11-01', '2007-12-01')
        period_9 = Period('2007-09-01', '2007-10-01')

        multi_period.add(period_1)
        periods = multi_period.get_periods()
        self.assertEqual(1, len(periods))
        self.assertEqual(period_1, periods[0])

        multi_period.add(period_1)
        periods = multi_period.get_periods()
        self.assertEqual(1, len(periods))
        self.assertEqual(period_1, periods[0])

        multi_period.add(period_2)
        periods = multi_period.get_periods()
        self.assertEqual(1, len(periods))
        self.assertEqual(Period('2007-01-01', '2007-03-01'), periods[0])

        multi_period.add(period_3)
        periods = multi_period.get_periods()
        self.assertEqual(1, len(periods))
        self.assertEqual(Period('2006-12-01', '2007-03-01'), periods[0])

        multi_period.add(period_4)
        periods = multi_period.get_periods()
        self.assertEqual(1, len(periods))
        self.assertEqual(Period('2006-11-01', '2007-03-01'), periods[0])

        multi_period.add(period_5)
        periods = multi_period.get_periods()
        self.assertEqual(1, len(periods))
        self.assertEqual(Period('2006-11-01', '2007-04-01'), periods[0])

        multi_period.add(period_6)
        periods = multi_period.get_periods()
        self.assertEqual(2, len(periods))
        self.assertEqual(Period('2006-11-01', '2007-04-01'), periods[0])
        self.assertEqual(period_6, periods[1])

        multi_period.add(period_7)
        periods = multi_period.get_periods()
        self.assertEqual(1, len(periods))
        self.assertEqual(Period('2006-11-01', '2007-06-01'), periods[0])

        multi_period.add(period_5)
        periods = multi_period.get_periods()
        self.assertEqual(1, len(periods))
        self.assertEqual(Period('2006-11-01', '2007-06-01'), periods[0])

        multi_period.add(period_8)
        periods = multi_period.get_periods()
        self.assertEqual(2, len(periods))
        self.assertEqual(Period('2006-11-01', '2007-06-01'), periods[0])
        self.assertEqual(period_8, periods[1])

        multi_period.add(period_9)
        periods = multi_period.get_periods()
        self.assertEqual(3, len(periods))
        self.assertEqual(Period('2006-11-01', '2007-06-01'), periods[0])
        self.assertEqual(period_9, periods[1])
        self.assertEqual(period_8, periods[2])