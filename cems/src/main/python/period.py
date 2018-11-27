import datetime


class Period:
    def __init__(self, start_date, end_date):
        """

        :raise exceptions.ValueError: If the start date is not less than the end date.
        """
        if isinstance(start_date, datetime.date):
            a = start_date
        elif isinstance(start_date, str):
            a = self.__from_iso_format(start_date)
        else:
            a = datetime.date(start_date[0], start_date[1], start_date[2])
        if isinstance(end_date, datetime.date):
            b = end_date
        elif isinstance(end_date, str):
            b = self.__from_iso_format(end_date)
        else:
            b = datetime.date(end_date[0], end_date[1], end_date[2])
        if a <= b:
            self.start_date = a
            """:type : datetime.date"""
            self.end_date = b
            """:type : datetime.date"""
        else:
            raise ValueError("The start date must be less than the end date." + a.isoformat() + ' - ' + b.isoformat())

    def get_start_date(self):
        """

        :rtype : datetime.date
        """
        return self.start_date

    def get_end_date(self):
        """

        :rtype : datetime.date
        """
        return self.end_date

    def get_intersection(self, other):
        """

        :type other: Period
        :rtype : Period
        """
        if self.is_intersecting(other):
            start_date = self.get_start_date()
            if start_date < other.get_start_date():
                start_date = other.get_start_date()
            end_date = self.get_end_date()
            if end_date > other.get_end_date():
                end_date = other.get_end_date()
            intersection = Period(start_date, end_date)
        else:
            intersection = None
        return intersection

    def is_including(self, other):
        """

        :type other: Period
        :rtype : bool
        """
        return self.get_start_date() <= other.get_start_date() and self.get_end_date() >= other.get_end_date()

    def is_intersecting(self, other):
        """

        :type other: Period
        :rtype : bool
        """
        return self.get_start_date() < other.get_end_date() and self.get_end_date() > other.get_start_date()

    def is_connecting(self, other):
        """

        :type other: Period
        :rtype : bool
        """
        return self.get_start_date() == other.get_end_date() or self.get_end_date() == other.get_start_date()

    def grow(self, other):
        """

        :type other: Period
        :rtype : bool
        """
        grown = False
        if self.is_intersecting(other) or self.is_connecting(other):
            if other.get_start_date() < self.get_start_date():
                self.start_date = other.get_start_date()
                grown = True
            if other.get_end_date() > self.get_end_date():
                self.end_date = other.get_end_date()
                grown = True
        return grown

    @staticmethod
    def __from_iso_format(iso_string):
        """

        :type iso_string: str
        :rtype: datetime.date
        """
        iso_parts = iso_string.split('-')
        return datetime.date(int(iso_parts[0]), int(iso_parts[1]), int(iso_parts[2]))

    def __eq__(self, other):
        """

        :type other: Period
        :rtype : bool
        """
        return self.get_start_date() == other.get_start_date() and self.get_end_date() == other.get_end_date()

    def __ne__(self, other):
        """

        :type other: Period
        :rtype: bool
        """
        return not self.__eq__(other)

    def __gt__(self, other):
        """

        :type other: Period
        :rtype: bool
        """
        return self.get_start_date() > other.get_start_date() or (
            self.get_start_date() == other.get_start_date() and self.get_end_date() > other.get_end_date())

    def __ge__(self, other):
        """

        :type other: Period
        :rtype: bool
        """
        return self.__eq__(other) or self.__gt__(other)

    def __lt__(self, other):
        """

        :type other: Period
        :rtype: bool
        """
        return self.get_start_date() < other.get_start_date() or (
            self.get_start_date() == other.get_start_date() and self.get_end_date() < other.get_end_date())

    def __le__(self, other):
        """

        :type other: Period
        :rtype: bool
        """
        return self.__eq__(other) or self.__lt__(other)

    def __hash__(self):
        return self.get_start_date().__hash__() + 31 * self.get_end_date().__hash__()
