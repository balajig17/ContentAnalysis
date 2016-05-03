

from tika import language
import os


lang_prop = "/Users/Balaji/Repositories/tika/tika-core/src/main/resources/org/apache/tika/language/tika.language.properties"
lang_keys = {}

with open(lang_prop) as f:
    separator = "="
    for line in f:
        if separator in line:
            name,value = line.split(separator,1)
            lang_keys[name.strip()] = value.strip()

def get_file_lang(file_path):
    result = language.from_file(file_path)
    lang = lang_keys["name."+result]
    return lang

def get_text_lang(content):
    result = language.from_buffer(content)
    lang = lang_keys["name."+result]
    return lang


def main():
    folder_path = "/Users/Balaji/Desktop/CSCI-599/CrawlData/awang-amd-1/gov/nasa/gsfc/gcmd"
    for subdir,dirs,files in os.walk(folder_path):
        for filename in files:
            if(filename.startswith(".DS_Store")):
                continue
            abs_path = os.path.join(subdir,filename)
            lang = get_file_lang(abs_path)
            print lang+" " +str(abs_path)






if __name__ == '__main__':
    main()