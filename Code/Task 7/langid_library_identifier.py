

from tika import language
import langid
import os
import json


def preprocess(str):
    str = str.replace("<","").replace(">","").replace("/","").replace("\\n", "").replace("\\t", "").replace("+", " ")
    return str

def write_to_file(path,str):
    text_file = open(path, "w")
    text_file.write(str)
    text_file.close()

def main():
    root = "/Volumes/My Passport/processes/LanguageIdentifier/input/text"
    destination = "/Volumes/My Passport/processes/LanguageIdentifier/output/text"
    for subdir,dirs,files in os.walk(root):
        for filename in files:
            if(filename.startswith(".DS_Store")):
                continue
            try:

                abs_path = os.path.join(subdir,filename)
                content=""
                json_rep=""
                with open(abs_path) as file:
                    json_rep = json.load(file)
                print("Processing " + filename)
                content=json_rep['content']
                lang = langid.classify(preprocess(content))
                print("it is in " + lang[0])
                json_rep["metadata"]["lang"]=lang[0]
                write_to_file(os.path.join(destination,filename),json.dumps(json_rep, indent=4, sort_keys=True))

            except Exception as err:
                print("An issue occured")
                write_to_file(os.path.join(destination,filename),json.dumps(json_rep, indent=4, sort_keys=True))








if __name__ == '__main__':
    main()