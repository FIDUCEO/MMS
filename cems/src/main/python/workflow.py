class Workflow:
    def __init__(self, usecase, production_period=None):
        """

        :type usecase: str
        :type production_period: Period
        """
        self.usecase = usecase
        self.production_period = production_period
        self.samples_per_month = 300000
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
