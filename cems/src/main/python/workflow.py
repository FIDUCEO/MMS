import calendar

import datetime
from datetime import timedelta

from job import Job
from monitor import Monitor
from period import Period
from sensor import Sensor
from sensorpair import SensorPair


class Workflow:
    def __init__(self, usecase, time_slot_days, config_dir=None, production_period=None):
        """

        :type usecase: str
        :type production_period: Period
        """
        self.usecase = usecase
        self.production_period = production_period
        self.config_dir = config_dir
        self.samples_per_time_slot = 50000
        self.time_slot_days = time_slot_days
        self.primary_sensors = set()
        self.secondary_sensors = set()
        self.usecase_config = None
        self.input_dir = None

    def get_usecase(self):
        """

        :rtype : str
        """
        return self.usecase

    def set_usecase_config(self, usecase_config):
        """

        :type usecase_config: str
        """
        self.usecase_config = usecase_config

    def _get_config_dir(self):
        """

        :rtype : str
        """
        return self.config_dir

    def get_production_period(self):
        """

        :rtype : Period
        """
        return self.production_period

    def get_samples_per_time_slot(self):
        """

        :rtype : int
        """
        return self.samples_per_time_slot

    def set_samples_per_time_slot(self, samples_per_time_slot):
        """

        :type samples_per_time_slot: int
        """
        self.samples_per_time_slot = samples_per_time_slot

    def set_input_dir(self, input_dir):
        """

        :type input_dir: str
        """
        self.input_dir = input_dir

    def get_input_dir(self):
        """
        :type : input directory for post-processing
        :rtype : str
        """
        return self.input_dir

    def get_time_slot_days(self):
        """
        :type : days per processing time slot
        :rtype : int
        """
        return self.time_slot_days

    def add_primary_sensor(self, name, start_date, end_date, version=''):
        """

        :type name: str
        """
        period = Period(start_date, end_date)
        for sensor in self._get_primary_sensors():
            if sensor.get_name() == name and sensor.get_period().is_intersecting(period):
                raise ValueError("Periods of sensor '" + name + "' must not intersect.")
        if version == '':
            self.primary_sensors.add(Sensor(name, period))
        else:
            self.primary_sensors.add(Sensor(name, period, version))

    def _get_primary_sensors(self):
        """

        :rtype : list
        """
        return sorted(list(self.primary_sensors), reverse=True)

    def add_secondary_sensor(self, name, start_date, end_date, version=''):
        """

        :type name: str
        :type start_date: str
        :type end_date: str
        :type version: str
        """
        period = Period(start_date, end_date)
        for sensor in self._get_secondary_sensors():
            if sensor.get_name() == name and sensor.get_period().is_intersecting(period):
                raise ValueError("Periods of sensor '" + name + "' must not intersect.")

        if version == '':
            self.secondary_sensors.add(Sensor(name, period))
        else:
            self.secondary_sensors.add(Sensor(name, period, version))

    def _get_secondary_sensors(self):
        """

        :rtype : list
        """
        return sorted(list(self.secondary_sensors), reverse=True)

    def _get_sensor_pairs(self):
        """

        :rtype : list
        """
        sensor_pairs = set()
        primary_sensors = self._get_primary_sensors()
        secondary_sensors = self._get_secondary_sensors()
        if len(secondary_sensors) > 0:
            for p in primary_sensors:
                for s in secondary_sensors:
                    if p != s:
                        try:
                            sensor_pair = SensorPair(p, s, self.get_production_period())
                            sensor_pairs.add(sensor_pair)
                        except ValueError:
                            pass
        else:
            for p in primary_sensors:
                try:
                    sensor_pair = SensorPair(p, p, self.get_production_period())
                    sensor_pairs.add(sensor_pair)
                except ValueError:
                    pass
        return sorted(list(sensor_pairs), reverse=True)

    def _get_data_period(self):
        """

        :rtype : Period
        """
        start_date = datetime.date.max
        end_date = datetime.date.min
        for sensor_pair in self._get_sensor_pairs():
            period = sensor_pair.get_period()
            if period.get_start_date() < start_date:
                start_date = period.get_start_date()
            if period.get_end_date() > end_date:
                end_date = period.get_end_date()
        if start_date < end_date:
            return Period(start_date, end_date)
        else:
            return None

    def _get_effective_production_period(self):
        """

        :rtype : Period
        """
        data_period = self._get_data_period()
        if data_period is None:
            return None
        production_period = self.get_production_period()
        if production_period is None:
            return data_period
        else:
            return production_period.get_intersection(data_period)

    def _add_inp_preconditions(self, preconditions):
        """

        :type preconditions: list
        :rtype : list
        """
        sensors = self._get_primary_sensors()
        for sensor in sensors:
            sensor_period = sensor.get_period()
            date = sensor_period.get_start_date() - datetime.timedelta(days=1)
            while date < sensor_period.get_end_date():
                chunk = self._get_next_period(date)
                if chunk is None:
                    break

                start_string = self._get_year_day_of_year(chunk.get_start_date())
                end_string = self._get_year_day_of_year(chunk.get_end_date())
                sensor_name = sensor.get_name()
                input_pre_condition = 'ingest-' + sensor_name + '-' + start_string + '-' + end_string
                preconditions.append(input_pre_condition)
                input_pre_condition = 'dummy_job-' + sensor_name + '-' + start_string + '-' + end_string
                preconditions.append(input_pre_condition)
                date = chunk.get_end_date()

        return preconditions

    def _add_post_processing_preconditions(self, preconditions):
        """

        :type preconditions: list
        :rtype : list
        """
        production_period = self.get_production_period()
        if production_period is None:
            return preconditions

        date = production_period.get_start_date()
        while date < production_period.get_end_date():
            chunk = self._get_next_period(date)

            start_string = self._get_year_day_of_year(chunk.get_start_date())
            end_string = self._get_year_day_of_year(chunk.get_end_date())

            mmd_pre_condition = 'mmd-' + start_string + '-' + end_string
            preconditions.append(mmd_pre_condition)

            date = chunk.get_end_date()

        return preconditions

    def _get_monitor(self, hosts, calls, log_dir, simulation, synchronous=False):
        """

        :type hosts: list
        :type calls: list
        :type log_dir: str
        :type simulation: bool
        :type synchronous: bool
        :rtype : Monitor
        """
        preconditions = list()

        # @todo 1 tb/tb refactor this, it's only relevant for ingestion
        self._add_inp_preconditions(preconditions)
        self._add_post_processing_preconditions(preconditions)

        # @todo 2 tb/tb do we need this 2016-03-29
        # self._add_obs_preconditions(preconditions)
        # self._add_smp_preconditions(preconditions)
        return Monitor(preconditions, self.get_usecase(), hosts, calls, log_dir, simulation, synchronous)

    def _next_year_start(self, date):
        """

        :type date: datetime.date
        :rtype : datetime.date
        """
        return datetime.date(date.year + 1, 1, 1)

    def _get_year_day_of_year(self, date):
        """

        :type date: datetime.date
        :rtype : str
        """
        return str(date.year) + '-' + str(date.timetuple().tm_yday).zfill(3)

    def _get_next_period(self, date):
        """

        :param date: datetime.date
        :return: Period
        """

        start = date + timedelta(1)
        end = date + timedelta(self.time_slot_days)
        if end.year > start.year:
            last_year = end.year - 1
            last_day = calendar.monthrange(last_year, 12)[1]
            end = datetime.date(last_year, 12, last_day)
        elif end.month > start.month:
            month_before = end.month - 1
            last_day = calendar.monthrange(end.year, month_before)[1]
            end = datetime.date(end.year, month_before, last_day)

        if end < start:
            return None

        return Period(start, end)

    def run_ingestion(self, hosts, num_parallel_tasks, simulation=False, logdir='trace'):
        """

        :param hosts: list
        :param num_parallel_tasks: int
        :param simulation: bool
        :param logdir: str
        :return:
        """
        monitor = self._get_monitor(hosts, [('ingest_start.sh', num_parallel_tasks)], logdir, simulation)

        sensors = self._get_primary_sensors()
        for sensor in sensors:
            sensor_period = sensor.get_period()
            date = sensor_period.get_start_date() - datetime.timedelta(days=1)
            data_version = sensor.get_data_version()
            while date < sensor_period.get_end_date():
                chunk = self._get_next_period(date)
                start_string = self._get_year_day_of_year(chunk.get_start_date())
                end_string = self._get_year_day_of_year(chunk.get_end_date())
                sensor_name = sensor.get_name()
                job_name = 'ingest-' + sensor_name + '-' + start_string + '-' + end_string
                post_condition = 'stored-' + sensor_name + '-' + start_string + '-' + end_string

                job = Job(job_name, 'ingest_start.sh', [job_name], [post_condition],
                          [sensor_name, start_string, end_string, data_version, self._get_config_dir()])
                monitor.execute(job)

                date = chunk.get_end_date()

        monitor.wait_for_completion()

    def run_test_job(self, hosts, simulation=False, logdir='trace'):
        """

        :param hosts: list
        :param logdir: str
        :param simulation: bool
        :return:
        """
        monitor = self._get_monitor(hosts, list(), logdir, simulation)

        sensors = self._get_primary_sensors()
        for sensor in sensors:
            sensor_period = sensor.get_period()
            date = sensor_period.get_start_date() - datetime.timedelta(days=1)
            data_version = sensor.get_data_version()
            while date < sensor_period.get_end_date():
                chunk = self._get_next_period(date)
                start_string = self._get_year_day_of_year(chunk.get_start_date())
                end_string = self._get_year_day_of_year(chunk.get_end_date())
                sensor_name = sensor.get_name()
                job_name = 'dummy_job-' + sensor_name + '-' + start_string + '-' + end_string
                post_condition = 'stored-' + sensor_name + '-' + start_string + '-' + end_string

                job = Job(job_name, 'dummy_job_start.sh', [job_name], [post_condition],
                          [sensor_name, start_string, end_string, data_version, self._get_config_dir()])
                monitor.execute(job)

                date = chunk.get_end_date()

        monitor.wait_for_completion()

    def run_matchup(self, hosts, num_parallel_tasks, simulation=False, logdir='trace', synchronous=False):
        """

        :param hosts: list
        :param num_parallel_tasks: int
        :param simulation: bool
        :param logdir: str
        :param synchronous: bool
        :return:
        """
        if synchronous:
            runs_script = 'matchup_run.sh'
        else:
            runs_script = 'matchup_start.sh'

        monitor = self._get_monitor(hosts, [(runs_script, num_parallel_tasks)], logdir, simulation, synchronous)

        sensors = self._get_sensor_pairs()
        for sensor_pair in sensors:
            name = sensor_pair.get_name()
            """:type : str"""
            sensor_period = sensor_pair.get_period()
            date = sensor_period.get_start_date() - datetime.timedelta(days=1)
            while date < sensor_period.get_end_date():
                chunk = self._get_next_period(date)
                start_string = self._get_year_day_of_year(chunk.get_start_date())
                end_string = self._get_year_day_of_year(chunk.get_end_date())

                job_name = 'matchup-' + name + '-' + start_string + '-' + end_string + '-' + self.usecase_config
                primary_name = sensor_pair.get_primary_name()
                pre_condition = 'ingest-' + primary_name + '-' + start_string + '-' + end_string
                post_condition = 'matchup-' + name + '-' + start_string + '-' + end_string + '-' + self.usecase_config

                job = Job(job_name, runs_script, [pre_condition], [post_condition],
                          [start_string, end_string, self._get_config_dir(), self.usecase_config])
                monitor.execute(job)

                date = chunk.get_end_date()

        monitor.wait_for_completion()

    def run_post_processing(self, hosts, num_parallel_tasks, simulation=False, logdir='trace', synchronous=False):
        """

        :param hosts: list
        :param num_parallel_tasks: int
        :param simulation: bool
        :param logdir: str
        :return:
        """

        if synchronous:
            runs_script = 'post_processing_run.sh'
        else:
            runs_script = 'post_processing_start.sh'

        monitor = self._get_monitor(hosts, [(runs_script, num_parallel_tasks)], logdir, simulation)
        production_period = self.get_production_period()
        date = production_period.get_start_date()
        while date < production_period.get_end_date():
            chunk = self._get_next_period(date)

            start_string = self._get_year_day_of_year(chunk.get_start_date())
            end_string = self._get_year_day_of_year(chunk.get_end_date())

            job_name = 'post-processing-' + start_string + '-' + end_string + '-' + self.usecase_config
            pre_condition = 'mmd-' + start_string + '-' + end_string
            post_condition = 'post-processing-' + start_string + '-' + end_string + '-' + self.usecase_config

            job = Job(job_name, 'post_processing_start.sh', [pre_condition], [post_condition],
                      [self.input_dir, start_string, end_string, self.usecase_config, self._get_config_dir()])
            monitor.execute(job)

            date = chunk.get_end_date()

        monitor.wait_for_completion()
