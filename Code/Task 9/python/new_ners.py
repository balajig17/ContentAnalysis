



import tika
from tika import parser
import json
import os


tika_url = "http://localhost:9998"
simple_mime_type="text"

local_path = "/Volumes/My Passport/processes/visualizer/input/task6/combined"
dest_path = os.getcwd()


def parse_all_files(path):
    total_newly_added=0
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
                ner_dict = find_new_NER(json_rep)
                json_rep,newly_added=find_total_and_append(json_rep,ner_dict)
                #writeFile(file_name,json.dumps(json_rep, indent=4))
                total_newly_added+=newly_added
            except Exception as e:
                pass

    print("Total newly added is " + str(total_newly_added))
    print ("Finished parsing files in directory.")



def writeFile(filename,data,):
    text_file = open(os.path.join(dest_path,filename), "w")
    text_file.write(data)
    text_file.close()


def find_new_NER(json_rep):
    file_ner_dict={}
    metadata = json_rep["metadata"]
    for key,val in metadata.items():
        if "NER" in key or "AGREED_NER" in key:
            arr = metadata[key]
            for entity in arr:
                if entity not in file_ner_dict:
                    file_ner_dict[entity]=0
                file_ner_dict[entity]=file_ner_dict[entity]+1
    return file_ner_dict




def find_total_and_append(json_rep,ner_dict):

    newly_added=0
    new_words=[]
    for key, val in ner_dict.items():
        if val >=2:
             newly_added+=1
             new_words.append(key)

    json_rep["metadata"]["NEW_NER"] = new_words
    return json_rep, newly_added



def main():
    pass
    parse_all_files(local_path)


if __name__ == "__main__":
    main()