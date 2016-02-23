import getopt
import os
import shutil

import sys


class ConvertHdf:

    list_ext = ['GC', 'WI', 'SV', 'MM']
    list__temp_ext=['l1c', 'l1b', 'log']

    def run(self, argv):
        self.check_aapp_scripts_installed()

        self.input_path, self.output_path = self.parse_cmd_line()

        if os.path.exists(self.input_path):
            files = [fn for fn in os.listdir(self.input_path) if any(fn.endswith(ext) for ext in self.list_ext)]
        else:
            print("no files found in input directory: " + self.input_path)
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

        self.delete_files()

    def check_aapp_scripts_installed(self):
        aapp_prefix = os.popen('echo $AAPP_PREFIX').read()
        if len(aapp_prefix) < 1:
            print("Install AAPP application and more details: http://nwpsaf.eu/deliverables/aapp/")
            sys.exit()

    def delete_files(self):
        try:
            files = [fn for fn in os.listdir('.') if any(fn.endswith(ext) for ext in self.list__temp_ext)]
            for _file in files:
                os.remove(_file)

        except NameError:
            print("Check All user")

    def parse_cmd_line(self):
        in_path = ''
        out_path = ''

        try:
            options, args = getopt.getopt(sys.argv[1:], 'i:o:', ['ifile=', 'ofile='])
        except getopt.GetoptError:
            self.print_usage_and_exit(2)

        for option, arg in options:
            if option == '-h':
                self.print_usage_and_exit(0)
            elif option in ('-i', '--ifile'):
                in_path = arg
            elif option in ('-o', '--ofile'):
                out_path = arg

        if in_path == '' or out_path == '':
            self.print_usage_and_exit(2)


        return [in_path, out_path]

    def print_usage_and_exit(self, exitcode):
        print('-i <inputDirectory> -o <outputDirectory>')
        sys.exit(exitcode)


if __name__ == "__main__":
    converter = ConvertHdf()
    converter.run(sys.argv[1:])
