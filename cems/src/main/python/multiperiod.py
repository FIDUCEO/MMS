from period import Period

class MultiPeriod:
    def __init__(self):
        self.periods = list()

    def add(self, other):
        """

        :type other: Period
        """
        added = False
        for period in self.periods:
            added = period.is_including(other)
            if added:
                break
            else:
                added = period.grow(other)
                if added:
                    self.__maintain_disjunctive_state(period)
                    break
        if not added:
            self.periods.append(Period(other.get_start_date(), other.get_end_date()))

    def get_periods(self):
        """

        :rtype : list
        """
        return sorted(self.periods)

    def __maintain_disjunctive_state(self, g):
        """

        :param g: The grown period.
        :type g: Period
        """
        trash = list()
        for p in self.periods:
            if g != p:
                if g.is_intersecting(p) or g.is_connecting(p):
                    g.grow(p)
                    trash.append(p)
        for p in trash:
            self.periods.remove(p)

