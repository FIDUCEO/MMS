from workflow import Workflow

w = Workflow('usecase35_ocean_rain_sst_mhs_n18', 7, '/group_workspaces/cems2/fiduceo/Software/mms/config')
w.add_primary_sensor('ocean-rain-sst', '2010-01-01', '2017-08-31', 'v1.0')
w.add_secondary_sensor('mhs-n18', '2005-05-25', '2016-03-04', 'v1.0')

w.set_usecase_config('usecase-35.xml')

w.run_matchup(hosts=[('localhost', 60)])