import exceptions

from period import Period
from sensor import Sensor


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