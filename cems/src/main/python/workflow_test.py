import datetime
import os
import unittest

from period import Period
from sensor import Sensor
from workflow import Workflow


class WorkflowTest(unittest.TestCase):
    logdir = 'test_log'

    def tearDown(self):
        if os.path.exists('test.report'):
            os.remove('test.report')

        if os.path.exists('test.status'):
            os.remove('test.status')

        if os.path.exists(self.logdir):
            os.rmdir(self.logdir)

    def test_get_usecase(self):
        w = Workflow('test', 1)
        self.assertEqual('test', w.get_usecase())

    def test_get_config_dir(self):
        w = Workflow('test', 1, '/some/where/config')
        self.assertEqual('/some/where/config', w._get_config_dir())


    def test_get_production_period(self):
        w = Workflow('test', 2, 'config/dir', Period((2001, 3, 24), (2001, 4, 12)))
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

    def test_add_get_primary_sensors_version(self):
        w = Workflow('test', 5)
        w.add_primary_sensor('atsr-e2', '1995-06-01', '1996-01-01', 'version_5')
        self.assertEqual([Sensor('atsr-e2', Period((1995, 6, 1), (1996, 1, 1)), 'version_5')], w._get_primary_sensors())

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

    def test_add_get_secondary_sensors_version(self):
        w = Workflow('test', 8)
        w.add_secondary_sensor('avhrr-n16', '1995-06-01', '1996-01-01', 'v_3')
        self.assertEqual([Sensor('avhrr-n16', Period((1995, 6, 1), (1996, 1, 1)), 'v_3')], w._get_secondary_sensors())

    def test_add_get_secondary_sensors_multiple(self):
        w = Workflow('test', 9)
        w.add_secondary_sensor('atsr-e2', '1996-06-01', '1997-01-01')
        w.add_secondary_sensor('atsr-e1', '1992-06-01', '1996-01-01')
        w.add_secondary_sensor('atsr-en', '1997-01-01', '1999-01-01')

        sensors = w._get_secondary_sensors()
        self.assertEqual([Sensor('atsr-en', Period((1997, 1, 1), (1999, 1, 1))),
                          Sensor('atsr-e2', Period((1996, 6, 1), (1997, 1, 1))),
                          Sensor('atsr-e1', Period((1992, 6, 1), (1996, 1, 1)))], sensors)

    def test_get_sensor_pairs(self):
        w = Workflow('test', 4)
        w.add_primary_sensor('avhrr.n10', (1986, 11, 17), (1991, 9, 16))
        w.add_primary_sensor('avhrr.n11', (1988, 11, 8), (1994, 12, 31))
        w.add_primary_sensor('avhrr.n12', (1991, 9, 16), (1998, 12, 14))
        w.add_secondary_sensor('avhrr.n10', (1986, 11, 17), (1991, 9, 16))
        w.add_secondary_sensor('avhrr.n11', (1988, 11, 8), (1994, 12, 31))
        w.add_secondary_sensor('avhrr.n12', (1991, 9, 16), (1998, 12, 14))

        sensor_pairs = w._get_sensor_pairs()
        self.assertEqual(2, len(sensor_pairs))
        self.assertEqual('avhrr.n12', sensor_pairs[0].get_primary_name())
        self.assertEqual('avhrr.n11', sensor_pairs[1].get_primary_name())

    def test_get_data_period(self):
        w = Workflow('test', 2)
        w.add_primary_sensor('avhrr.n10', (1986, 11, 17), (1991, 9, 16))
        w.add_primary_sensor('avhrr.n11', (1988, 11, 8), (1994, 12, 31))
        w.add_primary_sensor('avhrr.n12', (1991, 9, 16), (1998, 12, 14))
        w.add_secondary_sensor('avhrr.n10', (1986, 11, 17), (1991, 9, 16))
        w.add_secondary_sensor('avhrr.n11', (1988, 11, 8), (1994, 12, 31))
        w.add_secondary_sensor('avhrr.n12', (1991, 9, 16), (1998, 12, 14))

        data_period = w._get_data_period()
        self.assertEqual(datetime.date(1988, 11, 8), data_period.get_start_date())
        self.assertEqual(datetime.date(1994, 12, 31), data_period.get_end_date())

    def test_get_effective_production_period_empty_list(self):
        w = Workflow('test', 2)

        period = w._get_effective_production_period()
        self.assertEqual(period, None)

    def test_get_effective_production_period_one_sensor_interval(self):
        w = Workflow('test', 2)
        w.add_primary_sensor('avhrr.n11', (1988, 11, 8), (1994, 12, 31))

        period = w._get_effective_production_period()
        self.assertEqual(Period((1988, 11, 8), (1994, 12, 31)), period)

    def test_get_effective_production_period_one_sensor_with_workflow_period(self):
        w = Workflow('test', 2, 'config', Period((1994, 1, 1), (1994, 12, 31)))
        w.add_primary_sensor('avhrr.n11', (1988, 11, 8), (1994, 12, 31))

        period = w._get_effective_production_period()
        self.assertEqual(Period((1994, 1, 1), (1994, 12, 31)), period)

    def test_get_effective_production_period_many_sensors(self):
        w = Workflow('test', 2)
        w.add_primary_sensor('avhrr.n11', (1988, 11, 8), (1994, 12, 31))
        w.add_primary_sensor('avhrr.n12', (1991, 9, 16), (1998, 12, 14))
        w.add_secondary_sensor('avhrr.n10', (1986, 11, 17), (1991, 9, 16))

        period = w._get_effective_production_period()
        self.assertEqual(Period((1988, 11, 8), (1991, 9, 16)), period)

    def test_get_inp_preconditions_for_one_sensor(self):
        w = Workflow('test', 8)
        w.add_primary_sensor('avhrr.n10', (1986, 11, 17), (1991, 9, 16))

        preconditions = list()
        preconditions = w._add_inp_preconditions(preconditions)
        self.assertEqual(232, len(preconditions))
        self.assertEqual('ingest-avhrr.n10-1986-321-1986-328', preconditions[0])
        self.assertEqual('ingest-avhrr.n10-1989-048-1989-055', preconditions[108])
        self.assertEqual('ingest-avhrr.n10-1991-252-1991-259', preconditions[231])

    def test_get_inp_preconditions_for_two_sensors(self):
        w = Workflow('test', 14)
        w.add_primary_sensor('avhrr.n11', (1988, 12, 18), (1990, 8, 15))
        w.add_primary_sensor('avhrr.n12', (1985, 10, 16), (1988, 6, 23))

        preconditions = list()
        preconditions = w._add_inp_preconditions(preconditions)
        self.assertEqual(153, len(preconditions))
        self.assertEqual('ingest-avhrr.n12-1985-289-1985-302', preconditions[0])
        self.assertEqual('ingest-avhrr.n12-1986-210-1986-212', preconditions[27])
        self.assertEqual('ingest-avhrr.n12-1987-166-1987-179', preconditions[58])

    def test_next_year_start(self):
        w = Workflow('test', 2)
        date = datetime.date(2001, 10, 14)

        next = w._next_year_start(date)
        self.assertEqual(datetime.date(2002, 1, 1), next)

    def test_get_next_period(self):
        w = Workflow('test', 10)

        date = datetime.date(2001, 10, 14)
        next_period = w._get_next_period(date)
        self.assertEqual(Period((2001, 10, 15), (2001, 10, 24)), next_period)

    def test_get_next_period_cut_at_month_end(self):
        w = Workflow('test', 10)

        date = datetime.date(2001, 9, 22)
        next_period = w._get_next_period(date)
        self.assertEqual(Period((2001, 9, 23), (2001, 9, 30)), next_period)

        next_period = w._get_next_period(next_period.get_end_date())
        self.assertEqual(Period((2001, 10, 1), (2001, 10, 10)), next_period)

    def test_get_next_period_overlap_year(self):
        w = Workflow('test', 7)

        date = datetime.date(2001, 12, 26)
        next_period = w._get_next_period(date)
        self.assertEqual(Period((2001, 12, 27), (2001, 12, 31)), next_period)

        next_period = w._get_next_period(next_period.get_end_date())
        self.assertEqual(Period((2002, 1, 1), (2002, 1, 7)), next_period)

    def test_get_next_period_changed_period(self):
        w = Workflow('test', 5)

        date = datetime.date(2002, 11, 15)
        next_period = w._get_next_period(date)
        self.assertEqual(Period((2002, 11, 16), (2002, 11, 20)), next_period)

    def test_get_year_day_of_year(self):
        w = Workflow('test', 7)

        date = datetime.date(1989, 12, 9)
        self.assertEqual('1989-343', w._get_year_day_of_year(date))

        date = datetime.date(1989, 2, 7)
        self.assertEqual('1989-038', w._get_year_day_of_year(date))

    def test_get_monitor(self):
        w = Workflow('test', 10)

        monitor = w._get_monitor(list([('localhost', 10)]), list(), self.logdir, True)
        self.assertNotEqual(monitor, None)

    # def test_ingest_avhrr(self):
    #     w = Workflow('test', 11, 'config/dir')
    #     w.add_primary_sensor('avhrr-n12', '1995-06-01', '1996-06-05')
    #
    #     w.run_ingestion(list([('localhost', 5)]), True, self.logdir)
    #
    #     with open('test.status', 'r') as status:
    #         self.assertEqual('37 created, 0 running, 0 backlog, 37 processed, 0 failed\n', status.readline())
    #
    #     with open('test.report', 'r') as report:
    #         self.assertEqual(37, len(report.readlines()))

    # def test_ingest_avhrr_n07(self):
    #     w = Workflow('test', 7,'/group_workspaces/cems2/fiduceo/Software/mms/config')
    #     w.add_primary_sensor('avhrr-n07', '1981-09-01', '1985-01-30', 'v01.2')
    #
    #     w.run_ingestion(list([('localhost', 24)]), True, self.logdir)
    #
    #     with open('test.status', 'r') as status:
    #         self.assertEqual('203 created, 0 running, 0 backlog, 203 processed, 0 failed\n', status.readline())
    #
    #     with open('test.report', 'r') as report:
    #         self.assertEqual(203, len(report.readlines()))

    # def test_matchup_avhrr_n08_avhrr_n07(self):
    #     w = Workflow('test', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
    #     w.add_primary_sensor('avhrr-n18', '2008-05-01', '2008-05-31', 'v01.2')
    #     w.add_secondary_sensor('avhrr-n17', '2008-05-01', '2008-05-31', 'v01.2')
    #
    #     w.set_usecase_config('usecase-02.xml')
    #     w.run_matchup(list([('localhost', 4)]), True, self.logdir)
    #
    #     with open('test.status', 'r') as status:
    #         self.assertEqual('5 created, 0 running, 0 backlog, 5 processed, 0 failed\n', status.readline())
    #
    #     with open('test.report', 'r') as report:
    #         self.assertEqual(5, len(report.readlines()))

    # def test_post_processing_mmd6c(self):
    #     period = Period('2007-05-01', '2008-11-16')
    #     w = Workflow('test', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config', period)
    #
    #     w.set_usecase_config('post-processing_06x.xml')
    #     w.set_input_dir('/home/tom/the/data')
    #
    #     w.run_post_processing(list([('localhost', 4)]), True, self.logdir)
    #
    #     with open('test.status', 'r') as status:
    #         self.assertEqual('93 created, 0 running, 0 backlog, 93 processed, 0 failed\n', status.readline())
    #
    #     with open('test.report', 'r') as report:
    #         self.assertEqual(93, len(report.readlines()))

    def test_set_get_input_dir(self):
        w = Workflow('test', 4)
        w.set_input_dir('/usr/local/inputs')
        self.assertEqual('/usr/local/inputs', w.get_input_dir())

