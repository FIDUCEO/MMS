import getopt
import os
import shutil

import sys


class ConvertHdf:
    def __init__(self):
        list_ext = ['GC', 'WI', 'SV', 'MM']

        self.input_path, self.output_path = self.default_file()

        if os.path.exists(self.input_path):
            files = [fn for fn in os.listdir(self.input_path) if any(fn.endswith(ext) for ext in list_ext)]

        aapp_prefix = os.popen('echo $AAPP_PREFIX').read()
        if len(aapp_prefix) < 1:
            print("Install AAPP application and more details: http://nwpsaf.eu/deliverables/aapp/")
            sys.exit()

        # create a temp folder
        if not os.path.exists(self.output_path):
            os.mkdir(self.output_path)

        for _file in files:
            _des_path = os.path.abspath('.')
            _src_path = os.path.abspath(self.input_path + '/' + _file)
            shutil.copy(_src_path, _des_path)
            os.rename(_des_path + '/' + _file, _des_path + '/ambn.l1b')
            os.system('atovin AMSU-B')
            os.system('convert_to_hdf5 -c[9] ambn.l1c')

            if os.path.isfile('ambn.l1c.h5'):
                shutil.move('ambn.l1c.h5', self.output_path + '/' + _file + '.h5')

    def delete_files(self, list_ext=['l1c', 'l1b', 'log']):
        try:
            files = [fn for fn in os.listdir('.') if any(fn.endswith(ext) for ext in list_ext)]
            for _file in files:
                os.remove(_file)

        except NameError:
            print("Check All user")

    def default_file(self):
        input_path = ''
        out_path = ''
        try:
            options, args = getopt.getopt(sys.argv[1:], 'i:o:', ['ifile=', 'ofile='])
        except getopt.GetoptError:
            print('-i <inputfile> -o <outputfile>')
            sys.exit(2)

        for optn, arg in options:
            if optn == '-h':
                print('-i <inputfile> -o <outputfile>')
                sys.exit()
            elif optn in ('-i', '--ifile'):
                input_path = arg
            elif optn in ('-o', '--ofile'):
                out_path = arg
        return [input_path, out_path]


ConvertHdf().delete_files()
