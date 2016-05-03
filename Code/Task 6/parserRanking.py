import json
import sys
import os
from os import path



destination="/Volumes/My Passport/processes/visualizer/output/task6"
dict={};


class Parser:

    total_raw_content_size=0
    parsed_content_size=0
    parsed_metadata_size=0;
    name=""
    def __init__(self,name):
        self.name=name
    def add_raw_content_size(self,amount):
        self.total_raw_content_size+=amount;
    def add_parsed_content_size(self,amount):
        self.parsed_content_size+=amount;
    def add_parsed_metadata_size(self,amount):
        self.parsed_metadata_size+=amount








def writeFile(filename,data):
    text_file = open(filename, "w")
    text_file.write(data)
    text_file.close()


#Extracts bunch of metrics from the json file
def extract_information_from_json(filepath):
    with open(filepath) as file:
        try:
                extracted_json=json.load(file);
                parsed_metadata_size = len(str(extracted_json["metadata"]))
                if "content" in extracted_json:
                    parsed_content_size = len(str(extracted_json["content"]));
                if "Content-Type" in extracted_json["metadata"]:
                    mime_type = extracted_json["metadata"]["Content-Type"];
                raw_size=len(str(extracted_json["cca_data"]["response"]["body"]))
                return raw_size,mime_type,parsed_content_size,parsed_metadata_size,extract_parser_names(extracted_json),True;

        except Exception as e:
            print("Error happened")
            return "","","","","",False;


#Gathers the file size in bytes
def get_raw_size_from_file (filepath):
    with open(filepath) as file:
        return len(file.read());

#Extract all the parser names from json and combines them into single string
#that key is used as parsers
def extract_parser_names(json):
    if "X-Parsed-By" in json["metadata"]:
        parsers = json["metadata"]["X-Parsed-By"];
        return ",".join(parsers)
    else:
        return "N/A"

def create_or_merge_with_parser_id(parser_id,raw_file_size,parsed_content_size,parsed_metadata_size,mime_type):

    if mime_type not in dict:
        dict[mime_type]={}


    if(parser_id in dict.get(mime_type)):
        parser=dict.get(mime_type).get(parser_id)
        parser.add_raw_content_size(raw_file_size)
        parser.add_parsed_content_size(parsed_content_size)
        parser.add_parsed_metadata_size(parsed_metadata_size)
    else:
        parser = Parser(parser_id);
        parser.add_raw_content_size(raw_file_size)
        parser.add_parsed_content_size(parsed_content_size)
        parser.add_parsed_metadata_size(parsed_metadata_size)
        dict.get(mime_type)[parser_id]=parser;


def generate_vis_for_all_mimetypes():
    combined_json = []
    for mimetype,val in dict.items():
        combined_json.append(generate_vis_from_mimetype_2(mimetype))

    print(json.dumps(combined_json, indent=4, sort_keys=True))


def generate_vis_from_mimetype_2(mimetype):
    parsers = dict[mimetype];
    json_rep={}

    for key,value in parsers.items():
        parser = parsers.get(key)
        parser_dict = {}
        parser_dict["name"]=parser.name
        parser_dict["mime_type"]=mimetype
        parser_dict["raw_content"]=parser.total_raw_content_size
        parser_dict["extracted_content"]=parser.parsed_content_size
        parser_dict["extracted_metadata"] =parser.parsed_metadata_size
        splitted= mimetype.split("/")[1]
        splitted = splitted.replace("+","_")
        writeFile(os.path.join(destination,splitted+".json"),json.dumps(json_rep, indent=4, sort_keys=True))
        return parser_dict
    return "-1"


def generate_vis_from_mimetype(mimetype):
    parsers = dict.get(mimetype);
    json_rep={}
    json_rep["data"] = []

    delim=''
    for key,value in parsers.items():
        parser = parsers.get(key)
        parser_dict = {}
        parser_dict["name"]=parser.name
        parser_dict["raw_content"]=parser.total_raw_content_size
        parser_dict["extracted_content"]=parser.parsed_content_size
        parser_dict["extracted_metadata"] =parser.parsed_metadata_size
        json_rep["data"].append(parser_dict)
    #mimetype=mimetype.replace("/","_")
    splitted= mimetype.split("/")[1]
    splitted = splitted.replace("+","_")

    writeFile(os.path.join(destination,splitted+".json"),json.dumps(json_rep, indent=4, sort_keys=True))
    return json_rep

def generate_vis():
    #generate_vis_for_all_mimetypes();
    for mimetype,value in dict.items():
        generate_vis_from_mimetype(mimetype)
def main():
    root="/Volumes/My Passport/processes/visualizer/input/task6/combined"

    dirs=os.listdir(root);
    files = [f for f in os.listdir(root) if path.isfile(os.path.join(root,f))]
    for filename in files:
        full_path_extracted = os.path.join(root,filename);

        raw_size,mime_type,parsed_content_size,parsed_metadata_size,parser_id,ok=extract_information_from_json(full_path_extracted)
        if ok:
            create_or_merge_with_parser_id(parser_id,raw_size,parsed_content_size,parsed_metadata_size,mime_type)


    generate_vis()
if __name__ == "__main__":
    main()