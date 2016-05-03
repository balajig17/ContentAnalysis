import json
import glob
from collections import Counter

directory = glob.glob('/Users/ayesh/Desktop/ContentDetection/Assignment3/Measurement_files/html/*')
directory += glob.glob('/Users/ayesh/Desktop/ContentDetection/Assignment3/Measurement_files/text/*')
ner_measurements = []
domain_names = []
measurements = {}
names = set()
for each_file in directory:
	with open(each_file) as ip:
		data = json.load(ip)
	try:
		temp_store = data['metadata']['AGREED_NER_MEASUREMENTS']
		for store in temp_store:
			temp_l = store.strip().split(';')
			if temp_l[2] != 'N/A':
				names.add(temp_l[2])
				ner_measurements.append(store)
				if temp_l[2] not in measurements:
					measurements[temp_l[2]] = {temp_l[1]:[temp_l[0]]}
				else:
					if temp_l[1] in measurements[temp_l[2]].keys():
						measurements[temp_l[2]][temp_l[1]].append(temp_l[0])
					else:
						measurements[temp_l[2]][temp_l[1]] = [temp_l[0]]
	except KeyError:
		print 'No NER Measurements'

print measurements
data = measurements
for types in measurements:
	print types
	for vals in measurements[types]:
		c = Counter(measurements[types][vals])
		print c
		data[types][vals] = dict(c)

with open('all_hierarchial.json','w') as op:
	op.write(json.dumps(data))

