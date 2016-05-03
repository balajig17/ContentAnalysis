import solr
from collections import Counter
import json

solrHandle = solr.Solr("http://localhost:8983/solr/NewCore")
documents = []
select = solr.SearchHandler(solrHandle,"/select")
startIndex = 0
rowCount = 1000
print "Starting.."
while(True):
	#Reading results from Solr.
	response = select.__call__(q="*",fields="file_url", start=startIndex, rows= rowCount)
	startIndex += rowCount
	if len(response.results) > 0:
		for doc in response.results:
					fname = doc['Content-Type']
					documents.append(fname)
	else:
		break
print "Finished Reading Docs.."

docLength = len(documents)
count = Counter(documents)
with open('original_trex_data.txt','r') as d:
	original_data = json.load(d)

response = []
for x in count:
	m,n = 0,0
	try:
		m = int(original_data[x])
		n = int(count[x])
	except:
		"meh"

	response.append({str('type'):str("'"+x+"'"),str('freq'):{str('low'):m,str('mid'):n}})

with open('both11.json','w') as g:
	g.write(json.dumps(response))
labels = []
solr = []
original = []
for x in count:
	m,n = 0,0
	try:
		m = int(original_data[x])
		n = int(count[x])
	except:
		"meh"
	labels.append(x)
	solr.append(n)
	original.append(m)

op = []
op.append({"labels":labels,"series":[{"label":'solr data',"values":solr},{"label":'original data',"values":original}]})

with open('both11.json','w') as g:
	g.write(json.dumps(op))