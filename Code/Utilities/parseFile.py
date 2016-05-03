import tika
from tika import parser
import json
import os

tika_url = "http://localhost:9998"
simple_mime_type="text"

local_path = "/Volumes/My Passport/processes/Tika_Parsing/input/"+simple_mime_type
dest_path = "/Volumes/My Passport/processes/Tika_Parsing/output/"+simple_mime_type


def parse_all_files(path):
    for subdir, dirs, files in os.walk(path):
        folders = subdir.split(local_path)
        print ("Parsing all files in " + folders[1] + " directory.")
        sorted_files = files;
        sorted_files.sort()
        for file_name in sorted_files:
            if file_name.startswith(".DS_Store"):
                continue
            try:
                print("Processing " + file_name)
                file=open(os.path.join(subdir, file_name),"r")
                json_rep = json.load(file)
                str=json_rep['cca_data']['response']['body']

                file_data = parser.from_buffer(str, tika_url)
                file_handle = open(os.path.join(dest_path, file_name), "w")
                file_data['cca_data']=json_rep['cca_data'];
                file_data['cca_path']=""
                if 'cca_path' in json_rep:
                    file_data['cca_path']=json_rep['cca_path']

                file_handle.write(json.dumps(file_data, indent=4, sort_keys=True))



                #remove content of the cca
                file_handle.close()
            except Exception as e:
                pass

    print ("Finished parsing files in directory.")


def main():
    pass
    parse_all_files(local_path)


if __name__ == "__main__":
    main()