class Job:
    def __init__(self, name, call, preconditions, postconditions, parameters):
        """

        :type name: str
        :type call: str
        :type preconditions: list
        :type postconditions: list
        :type parameters: list
        """
        self.name = name
        self.call = call
        self.preconditions = preconditions
        self.postconditions = postconditions
        self.parameters = list()
        for p in parameters:
            if isinstance(p, str):
                self.parameters.append(p)
            else:
                self.parameters.append(str(p))

    def get_name(self):
        """

        :rtype : str
        """
        return self.name

    def get_call(self):
        """

        :rtype : str
        """
        return self.call

    def get_preconditions(self):
        """

        :rtype : list
        """
        return self.preconditions

    def get_postconditions(self):
        """

        :rtype : list
        """
        return self.postconditions

    def get_parameters(self):
        """

        :rtype : list
        """
        return self.parameters