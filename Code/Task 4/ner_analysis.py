import json
import os
from collections import Counter


folder_path="/Users/Balaji/Desktop/CSCI-599/HW3/Language_parsed_files"
file_data = {}


def write_results():
    ner_types = []
    nodes = []
    links = []
    with open("/Users/Balaji/Repositories/ContentAnalysis/data/nerdata.json","w") as out_file:
        for type in file_data.keys():
            ner_types.append(type)
            for ner_field in file_data[type].keys():
                if ner_field not in ner_types:
                    ner_types.append(ner_field)
        for node in ner_types:
            nodes.append({"name": node})
        for type in file_data.keys():
            for ner_field in file_data[type].keys():
                links.append({"source": ner_types.index(type), "target": ner_types.index(ner_field), "value": file_data[type][ner_field]})
        result = {"nodes": nodes,"links": links}
        out_file.write(json.dumps(result,indent=1))
        out_file.close()








def edit_fields(fields):
    fields_to_remove = ["lang","Content-Type","X-TIKA:parse_time_millis","X-Parsed-By","Content-Encoding","AGREED_FOUND","NLTK_FOUND","GROBIDQUANTITIES_FOUND","CORENLP_FOUND","OPENNLP_FOUND","title","dc:title"]
    for field in fields_to_remove:
        if field in fields:
            fields.remove(field)
    for index in range(0,len(fields)):
        if(fields[index].startswith("AGREED")):
            fields[index] = fields[index].split("_",1)[1]
    return fields


def read_data(file_path):
    with open(file_path) as f:
        try:
            json_data = json.load(f)
            mime_type = json_data['metadata']['Content-Type']
            mime_type = mime_type.split(";", 1)[0]
            fields = json_data['metadata'].keys()
            fields = edit_fields(fields)
            if mime_type in file_data.keys():
                file_data[mime_type].update(fields)
            else:
                file_data[mime_type]=Counter(fields)
        except:
            pass

def main():
    for subdir,dirs,files in os.walk(folder_path):
        for file_name in files:
            if file_name.startswith(".DS_Store"):
                continue
            read_data(os.path.join(subdir, file_name))
    write_results()

if __name__ == '__main__':
    main()