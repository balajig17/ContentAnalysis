import json
import os
import re
import unicodedata


desired_count=3000;
current_count=0;

desired_mimetype="application/pdf"
desired_extension="pdf"



def remove_control_characters(s):
    return "".join(ch for ch in s if unicodedata.category(ch)[0]!="C")



#Assumption every teams file contains the path 572- , does not work if path earlier has the same key
def extractDomains(file_full_path):
    folder_start_key="572-"
    startIndex=file_full_path.find(folder_start_key)
    #get the next separator near it
    if startIndex!=-1:
        first_separator=file_full_path.find(os.sep,startIndex)
        if first_separator+1 <len(file_full_path) and first_separator!=-1:
            domain_path = file_full_path[first_separator+1:]
            domains = domain_path.split(os.sep);
            return domains



    return ""

def writeFile(filename,data,domains):
    text_file = open(filename, "w")
    json_output='{"cca_data":'+data+","+ '"cca_path":"'
    delim=""
    for domain in domains:
        json_output+=delim+domain
        delim="/"
    json_output+='"}'
    text_file.write(json_output)
    text_file.close()




def main():
    current_count=0;
    files_with_response=0;
    v=0;
    checked=0;
    rootdir = "/Volumes/My Passport/crawldata"
    destination="/Volumes/My Passport/processes/CCA_Extraction/output/pdf"
    for folder, subs, files in os.walk(rootdir):
            if current_count>=desired_count:
                   break;
            for filename in files:
                if current_count>=desired_count:
                   break;

                with open(os.path.join(folder, filename), 'r',encoding='utf8',errors='ignore') as src:
                   #print("checking " + filename )
                   if filename[0]!=".":
                       #checked+=1
                       full_path=os.path.join(folder, filename)
                       try:
                        regex=re.compile(".*?(\{.*\})",re.DOTALL)
                        text_data=src.read()
                        match =regex.search(text_data)
                        if len(match.groups())>0:
                            matched = match.group(1)
                            #matched_json = matched.encode("utf-8", errors="ignore")
                            #matched_json=matched_json.decode("utf-8", errors="ignore")
                            valid_json=json.loads(remove_control_characters(matched))
                            v+=1;
                            content=valid_json['response']['body']
                            response_status = valid_json['response']['status']
                            for item in valid_json['response']['headers']:

                                if "Content-Type" in item :
                                    if item[1]== desired_mimetype or True :
                                        head, tail = os.path.split(full_path);
                                        extension=tail.split(".")[-1]

                                        if (extension==desired_extension)and len(content)>10 :
                                            created_json={};
                                            domains=extractDomains(full_path)
                                            current_count+=1
                                            #print(full_path +" is valid JSON")
                                            writeFile(destination+"/"+tail,remove_control_characters(matched),domains)
                                            print("Gathered " + str(current_count)+ " / " +str(desired_count)  )
                       except Exception as err:
                          x=9
                          #print(filename+ "NOT VALID JSON")





if __name__ == "__main__":
    main();