import getopt
import os
import shutil
import sys
import unittest


def list_files(files):
    file_list = os.listdir(files)
    for _file in file_list:
        print(_file)


def create_folder(_folder_name):
    try:
        os.mkdir(_folder_name)
    except OSError:
        print('The folder exist')


def move_file_and_rename(_old, _new):
    try:
        os.rename(_old, _new)
    except OSError:
        print('The folder exist')


def delete_files(list_ext=['l1c', 'l1b', 'log']):
    try:
        files = [fn for fn in os.listdir('.') if any(fn.endswith(ext) for ext in list_ext)]
        for _file in files:
            os.remove(_file)
    except NameError:
        print("Check All user")


class MyTest(unittest.TestCase):
    def test_default_path(self):
        input_path = "."
        out_path = "."
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

        self.assertTrue(os.path.exists(input_path))
        self.assertTrue(os.path.exists(out_path))

    def test_installationAAPP(self):
        b = os.popen("echo $AAPP_PREFIX").read()
        print(b)
        self.failIf(len(b) < 0)

    def test_amsu_h5(self):
        list_ext = ['GC', 'WI', 'SV', 'MM']
        path = './amsu_b_class'
        self.temp_ = path + '/HDF_File'
        # check path
        if os.path.exists(path):
            files = [fn for fn in os.listdir(path) if any(fn.endswith(ext) for ext in list_ext)]

        self.failIf(len(files) < 1)

        # create a temp folder
        if not os.path.exists(self.temp_):
            os.mkdir(self.temp_)

        self.failIfEqual(os.path.exists(self.temp_), False)

        for _file in files:
            _des_path = os.path.abspath('.')
            _src_path = os.path.abspath(path + '/' + _file)
            shutil.copy(_src_path, _des_path)
            os.rename(_des_path + '/' + _file, _des_path + '/ambn.l1b')
            os.system('atovin AMSU-B')
            os.system('convert_to_hdf5 -c[9] ambn.l1c')

            self.failIfEqual(os.path.isfile('ambn.l1c.h5'), False, 'The convert to ')

            if os.path.isfile('ambn.l1c.h5'):
                shutil.move('ambn.l1c.h5', self.temp_ + '/' + _file + '.h5')


if __name__ == '__main__':
    unittest.main()
