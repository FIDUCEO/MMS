import os
import shutil
import sys
import re


def archive_ssmt2_files(input_dir, archive_dir):
    expr = re.compile('^F\d{14}\.nc$')
    for dir, dirs, files in os.walk(input_dir):
        files = filter(lambda f: expr.match(f), files)
        for file in files:
            sattelite = file[:3].lower()
            version = 'v01'
            year = file[3:7]
            month = file[7:9]
            day = file[9:11]

            sensor = 'ssmt2-' + sattelite

            src_file = os.path.join(dir, file)
            target_dir = create_target_path(archive_dir, [sensor, version, year, month, day])
            target_file = os.path.join(target_dir, file)
            print 'move "' + file + '" to ' + target_file
            if not os.path.exists(target_dir):
                os.makedirs(target_dir)
            shutil.move(src_file, target_file)


def create_target_path(dir, paths):
    assembly_path = dir
    for path_element in paths:
        assembly_path = os.path.join(assembly_path, path_element)
    return assembly_path


if __name__ == "__main__":
    args = sys.argv[1:]
    num_args = len(args)
    if num_args <> 2:
        print "Number of arguments must be 2 but is " + str(num_args)
        print 'List of arguments = ' + str(args)
        sys.exit(1)

    input_dir = args[0]
    archive_dir = args[1]

    if not os.path.isdir(input_dir):
        print("input path '" + input_dir + "' does not exist")
        sys.exit(1)

    if not os.path.isdir(archive_dir):
        print("output path '" + archive_dir + "' does not exist")
        sys.exit(1)

    archive_ssmt2_files(input_dir, archive_dir)
