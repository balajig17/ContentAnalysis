import os
import json

root = "/Volumes/My Passport/processes/visualizer/input/task8/combined"
destination = os.getcwd()
language_conversion = {"de":"GERMAN","it":"ITALIAN","ja":"JAPANESE","la":"LATIN","tr":"TURKISH","sl":"SLOVENE","ru":"RUSSIA","eu":"BASQUE","en":"ENGLISH","zh":"CHINESE","fa":"PERSIAN","pt":"PORTUGESE","nl":"DUTCH","eo":"ESPERANTO","fr":"FRENCH","es":"SPANISH","lb":"LUXEMBOURGISH"}


class SingleFile:
    def __init__(self, name, count):
        self.name = name
        self.count = count


def get_files_with_most_NER():
    singlefiles = []
    for subdir, dirs, files in os.walk(root):
        for filename in files:
            if (filename.startswith(".DS_Store")):
                continue
            try:
                abs_path = os.path.join(subdir, filename)
                content = ""
                json_rep = ""
                with open(abs_path) as file:
                    json_rep = json.load(file)
                metadata = json_rep["metadata"]
                total_sum = 0;
                for key, val in metadata.items():
                    if "_FOUND" in key:
                        total_sum += int(val)
                single_file = SingleFile(abs_path, total_sum)
                singlefiles.append(single_file)


            except Exception as err:
                print("An issue occured")
    sorted_single_files = sorted(singlefiles, key=lambda k: k.count, reverse=True)
    return sorted_single_files[:500]


print("Finished parsing files in directory.")


def generate_data_for_NER_visualization(files_with_most_NER):
    recogniser_dict = {}
    filenames = []
    for file in files_with_most_NER:
        json_rep = ""
        full_path = file.name
        head, tail = os.path.split(full_path)
        filenames.append(tail)
        with open(full_path) as file:
            json_rep = json.load(file)
        metadata = json_rep["metadata"]
        for key, val in metadata.items():
            if "_FOUND" in key:
                recogniser_name = key.split("_")[0]
                if recogniser_name not in recogniser_dict:
                    recogniser_dict[recogniser_name] = []
                recogniser_dict[recogniser_name].append(int(val))
    return filenames,recogniser_dict

def organize_data(filenames,recogniser_dict):

    json_out  = {}
    json_out["labels"]=[]
    for file in filenames:
        json_out["labels"].append(file)
    json_out["series"] = []
    for key,val in recogniser_dict.items():
        dict = {}
        dict["name"]=key
        dict["value"]=val
        json_out["series"].append(dict)
    #print(json.dumps(json_out, indent=4, sort_keys=True))
    return json_out


def generate_NER_visualization(files_with_most_NER):
    filenames,recogniser_dict = generate_data_for_NER_visualization(files_with_most_NER)
    json_out = organize_data(filenames,recogniser_dict)
    json_out=json.dumps(json_out, indent=4, sort_keys=True)
    writeFile("Ner_vis.json",json_out)


def writeFile(filename,data):
    text_file = open(os.path.join(destination,filename), "w")
    text_file.write(data)
    text_file.close()



def extract_data_for_wordcloud(type):

    lang_counts = {}
    metadata_count = {}
    singlefiles = []
    for subdir, dirs, files in os.walk(root):
        for filename in files:
            if (filename.startswith(".DS_Store")):
                continue
            try:
                abs_path = os.path.join(subdir, filename)
                content = ""
                json_rep = ""
                with open(abs_path) as file:
                    json_rep = json.load(file)
                metadata = json_rep["metadata"]

                if type == "lang":
                    process_languages(lang_counts,metadata)
                elif  type =="metadata":
                    process_metadata(metadata_count,metadata)






            except Exception as err:
                print("An issue occured")
    if type=="lang":
        generate_word_cloud_data_from_language(lang_counts,1,320)
    else :
        generate_word_cloud_data_from_metadata(metadata_count,0.05,-1)

def process_languages(lang_counts,metadata):

        if "lang" in metadata:
            language = metadata["lang"]
            if language not in lang_counts:
                lang_counts[language]=0
            lang_counts[language]= lang_counts[language]+1;


def process_metadata(metadata_count,metadata):
    for key, val in metadata.items():
        if key not  in metadata_count:
            metadata_count[key]=0
        metadata_count[key]= metadata_count[key]+1;


def generate_word_cloud_data_from_language(dict,scale,treshold):
    wordCloud= [];
    for key,val in dict.items():
        new_dict = {}
        new_dict["text"] = key
        new_dict["size"] = val*scale;
        if treshold!=-1 and val*scale>treshold:
            new_dict["size"] =treshold

        wordCloud.append(new_dict)
    json_string = json.dumps(wordCloud)
    writeFile("wordCloud_language.json",json_string)



def generate_word_cloud_data_from_metadata(dict,scale,treshold):
    wordCloud= [];
    for key,val in dict.items():
        new_dict = {}
        if key in  language_conversion:
            key = language_conversion[key]
        new_dict["text"] = key
        new_dict["size"] = val*scale;
        if treshold!=-1 and val*scale>treshold:
            new_dict["size"] =treshold

        wordCloud.append(new_dict)
    json_string = json.dumps(wordCloud)
    #print(json_string)
    writeFile("wordCloud_metadata.json",json_string)









def main():
    files_with_most_NER = get_files_with_most_NER()
    generate_NER_visualization(files_with_most_NER)
    extract_data_for_wordcloud("lang")
    extract_data_for_wordcloud("metadata")


if __name__ == "__main__":
    main()
