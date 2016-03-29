import calendar
import datetime
import exceptions
from datetime import timedelta

from monitor import Monitor
from period import Period
from sensor import Sensor
from sensorpair import SensorPair


class Workflow:
    def __init__(self, usecase, time_slot_days, production_period=None):
        """

        :type usecase: str
        :type production_period: Period
        """
        self.usecase = usecase
        self.production_period = production_period
        self.samples_per_time_slot = 50000
        self.time_slot_days = time_slot_days
        self.primary_sensors = set()
        self.secondary_sensors = set()

    def get_usecase(self):
        """

        :rtype : str
        """
        return self.usecase

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

    def get_time_slot_days(self):
        """
        :type : days per processing time slot
        :rtype : int
        """
        return self.time_slot_days

    def add_primary_sensor(self, name, start_date, end_date):
        """

        :type name: str
        """
        period = Period(start_date, end_date)
        for sensor in self._get_primary_sensors():
            if sensor.get_name() == name and sensor.get_period().is_intersecting(period):
                raise exceptions.ValueError, "Periods of sensor '" + name + "' must not intersect."
        self.primary_sensors.add(Sensor(name, period))

    def _get_primary_sensors(self):
        """

        :rtype : list
        """
        return sorted(list(self.primary_sensors), reverse=True)

    def add_secondary_sensor(self, name, start_date, end_date):
        """

        :type name: str
        """
        period = Period(start_date, end_date)
        for sensor in self._get_secondary_sensors():
            if sensor.get_name() == name and sensor.get_period().is_intersecting(period):
                raise exceptions.ValueError, "Periods of sensor '" + name + "' must not intersect."
        self.secondary_sensors.add(Sensor(name, period))

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
                        except exceptions.ValueError:
                            pass
        else:
            for p in primary_sensors:
                try:
                    sensor_pair = SensorPair(p, p, self.get_production_period())
                    sensor_pairs.add(sensor_pair)
                except exceptions.ValueError:
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

    def _get_monitor(self, hosts, calls, log_dir, simulation):
        """

        :type hosts: list
        :type calls: list
        :type log_dir: str
        :type simulation: bool
        :rtype : Monitor
        """
        preconditions = list()
        # @todo 2 tb/tb do we need this 2016-03-29
        # self._add_inp_preconditions(preconditions)
        # self._add_obs_preconditions(preconditions)
        # self._add_smp_preconditions(preconditions)
        return Monitor(preconditions, self.get_usecase(), hosts, calls, log_dir, simulation)

    def _next_year_start(self, date):
        """

        :type date: datetime.date
        :rtype : datetime.date
        """
        return datetime.date(date.year + 1, 1, 1)

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

        return Period(start, end)

    def run_ingestion(self, hosts, log_dir, simulation=False):
        """

        :param hosts: list
        :param log_dir: str
        :param simulation: bool
        :return:
        """
        monitor = self._get_monitor(hosts, list(), log_dir, simulation)

        sensors = self._get_primary_sensors()
        for sensor in sensors:
            sensor_period = sensor.get_period()
            date = sensor_period.get_start_date()
            while date < sensor_period.get_end_date():
                chunk = self._get_next_period(date)
                job_name = 'ingest-' + sensor.get_name() + '-' + str(date.year) + '-' + str(date.month) + '-' + str(date.day)
                print(job_name)
                # Job()
                date = chunk.get_end_date()

        monitor.wait_for_completion_and_terminate()
