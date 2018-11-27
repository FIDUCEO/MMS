

class SensorPair:
    def __init__(self, primary_sensor, secondary_sensor, production_period=None):
        """

        :type primary_sensor: Sensor
        :type secondary_sensor: Sensor
        :type production_period: Period
        :raise exceptions.ValueError: If the periods of primary and secondary sensors do not overlap.
        """
        if not primary_sensor.get_period().is_intersecting(secondary_sensor.get_period()):
            raise ValueError("The periods of primary and secondary sensors do not overlap.")
        if production_period is not None:
            if not primary_sensor.get_period().is_intersecting(production_period):
                raise ValueError("The periods of primary sensor and production do not overlap.")
            if not secondary_sensor.get_period().is_intersecting(production_period):
                raise ValueError("The periods of secondary sensor and production do not overlap.")
        self.primary_sensor = primary_sensor
        self.secondary_sensor = secondary_sensor

        if primary_sensor.get_name() != secondary_sensor.get_name():
            self.name = primary_sensor.get_name() + ',' + secondary_sensor.get_name()
        else:
            self.name = primary_sensor.get_name()

        self.period = primary_sensor.get_period().get_intersection(secondary_sensor.get_period())

        if production_period is not None:
            self.period = self.period.get_intersection(production_period)

    def get_name(self):
        """

        :rtype : str
        """
        return self.name

    def get_primary_name(self):
        """

        :rtype : str
        """
        return self.primary_sensor.get_name()

    def get_secondary_name(self):
        """

        :rtype : str
        """
        return self.secondary_sensor.get_name()

    def get_primary(self):
        """

        :rtype : Sensor
        """
        return self.primary_sensor

    def get_secondary(self):
        """

        :rtype : Sensor
        """
        return self.secondary_sensor

    def get_period(self):
        """

        :rtype : Period
        """
        return self.period

    def __eq__(self, other):
        """

        :type other: SensorPair
        :return: bool
        """
        return (self.get_primary() == other.get_primary() and self.get_secondary() == other.get_secondary()) or (
            self.get_primary() == other.get_secondary() and self.get_secondary() == other.get_primary())

    def __ne__(self, other):
        """

        :type other: SensorPair
        :return: bool
        """
        return not self.__eq__(other)

    def __ge__(self, other):
        """

        :type other: SensorPair
        :return: bool
        """
        return self.__eq__(other) or self.__gt__(other)

    def __gt__(self, other):
        """

        :type other: SensorPair
        :return: bool
        """
        return self.__ne__(other) and (self.get_primary() > other.get_primary() or (
            self.get_primary() == other.get_primary() and self.get_secondary() > other.get_secondary()))

    def __le__(self, other):
        """

        :type other: SensorPair
        :return: bool
        """
        return self.__eq__(other) or self.__lt__(other)

    def __lt__(self, other):
        """

        :type other: SensorPair
        :return: bool
        """
        return self.__ne__(other) and (self.get_primary() < other.get_primary() or (
            self.get_primary() == other.get_primary() and self.get_secondary() < other.get_secondary()))

    def __hash__(self):
        if self.get_primary() < self.get_secondary():
            return self.get_primary().__hash__() * 31 + self.get_secondary().__hash__()
        else:
            return self.get_secondary().__hash__() * 31 + self.get_primary().__hash__()
