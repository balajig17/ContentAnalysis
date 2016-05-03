import solr
import json
import requests
from os import listdir
from os.path import isfile, join
from os import walk



def metadata_score(metadata,doi=True,index_s=True):
    try:
        total_score = 0
        good_metadata_score = 0
        doi_score = 0
        index_score = 0
        good_metadata = ['Author','Content-Type','Last-Modified','Creation-Date','title']
        for m in metadata:
            if m in good_metadata:
                good_metadata_score += 1
        if doi:
            doi_score = 1
        if index_s:
            index_score += 1
        total_score = (good_metadata_score*0.12) + (doi_score*0.2) + (index_s*0.2)
        return total_score
    except KeyError:
        return 0



#http://localhost:8983/solr/#/polrsolr
s = solr.SolrConnection("http://localhost:8983/solr/polrsolr")

root = "/Volumes/My Passport/combinedData"
#root = "/Volumes/My Passport/Data/newSolrdata/geo"
files =[file for file in listdir(root) if isfile(join(root,file))]
totalNumOfFiles = sum([len(files) for r, d, files in walk(root)])
indexed = 0;
for d,ds,fs in walk(root):
     for f in fs:
       if(f[0]!='.'):
        print(str(indexed)+"/"+str(totalNumOfFiles) +" Indexed " +  join(d,f))

        jsonFile = open(join(d,f))
        #data=jsonFile.read().replace('\n', '')
        jsonParsed = json.load(jsonFile)
        if "content" in jsonParsed and len(jsonParsed["content"])>50:
            content = jsonParsed['content']
            metadata = jsonParsed['metadata'];
            metadata["content"] = content
            metadata['metadatascore']=metadata_score(metadata)
            metadata="["+str(metadata)+"]";
            metadata =  metadata.encode('utf-8');
            try:

                r=requests.post("http://localhost:8983/solr/polrsolr/update/json?commit=true",data=metadata)
            #print(r.status_code, r.reason)
            #print(metadata);
            except Exception as e:
                print(e)
                print("Exception occured")
            jsonFile.close();
            indexed+=1;










