import json
import os
from collections import Counter

folder_path = "/Users/Balaji/Desktop/CSCI-599/HW3/Language_parsed_files"
filetypes_data = {}

lang_prop = "/Users/Balaji/Desktop/CSCI-599/HW3/tika.language.properties"
lang_keys = {}

with open(lang_prop) as f:
    separator = "="
    for line in f:
        if separator in line:
            name, value = line.split(separator, 1)
            lang_keys[name.strip()] = value.strip()


def write_data():
    out_file = open("/Users/Balaji/Repositories/ContentAnalysis/data/dashboard.csv","w")
    out_file.write("File_Type")
    for lang in lang_keys.values():
        out_file.write(","+lang)
    out_file.write("\n")
    for file_type in filetypes_data:
        ct = Counter(filetypes_data[file_type])
        out_file.write(file_type)
        for lang in lang_keys.values():
            out_file.write(","+str(ct[lang]))
        out_file.write("\n")
    out_file.close()


def read_data(file_path):
    with open(file_path) as f:
        try:
            json_data = json.load(f)
            mime_type = json_data['metadata']['Content-Type']
            mime_type = mime_type.split(";",1)[0]
            lang_code = json_data['metadata']['lang']
            lang_name = lang_keys['name.' + lang_code]
            if mime_type in filetypes_data.keys():
                filetypes_data[mime_type].append(lang_name)
            else:
                filetypes_data[mime_type] = []
                filetypes_data[mime_type].append(lang_name)
        except:
            pass


def main():
    for subdir, dirs, files in os.walk(folder_path):
        for file_name in files:
            if file_name.startswith(".DS_Store"):
                continue
            read_data(os.path.join(subdir, file_name))
    write_data()


if __name__ == '__main__':
    main()
