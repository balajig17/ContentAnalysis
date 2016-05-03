import re

import solr
from collections import Counter
import json

def is_year(s):
	try:
		x = int(s)
		if x >= 1980 and x < 2017:
			return True
		else:
			return False
	except ValueError:
		return False

solrHandle = solr.Solr("http://localhost:8983/solr/NewCore")
select = solr.SearchHandler(solrHandle,"/select")
startIndex = 0
rowCount = 1000
print "Starting.."
dates = []
while(True):
	#Reading results from Solr.
	
	response = select.__call__(q="*", start=startIndex, rows= rowCount)
	startIndex += rowCount
	if len(response.results) > 0:
		for doc in response.results:
			#print doc
			#break
			try:
				fname = doc['Content-Type']
				fname = fname.encode('ascii')
				print fname
				fnameL = fname.split('/')
				if fnameL[0] == 'application':
					try:
						individual_dates = doc['NER_DATE']
						#print individual_dates
						for date in individual_dates:
							temp = ''.join(e for e in date if e.isalnum())
							if is_year(temp):
								dates.append(int(temp))
					except KeyError:
						print 'No NER_DATE'
			except KeyError:
				print 'No Conteny-Type'
	else:
		break
print "Finished Reading Docs.."

count = Counter(dates)
with open('part10_image.tsv','w') as vp: 
	vp.write('year\tfrequency\n')
	for c in count:
		vp.write(str(c)+"\t"+str(count[c])+"\n")

