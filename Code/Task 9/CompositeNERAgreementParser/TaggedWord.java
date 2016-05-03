package org.apache.tika.parser.CompositeNERAgreementParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TaggedWord 
{
	private String content;
	private Map<String,Integer> entityTypes;
	private ArrayList<String> recognisers;
	private int count;

	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Map<String, Integer> getEntityTypes() {
		return entityTypes;
	}

	public void setEntityTypes(Map<String, Integer> entityTypes) {
		this.entityTypes = entityTypes;
	}

	public ArrayList<String> getRecognisers() {
		return recognisers;
	}

	public void setRecognisers(ArrayList<String> recognisers) {
		this.recognisers = recognisers;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public TaggedWord(String content)
	{
		this.content=content;
		this.entityTypes=new HashMap<String,Integer>();
		this.count=0;
		this.recognisers= new ArrayList<String>();
		
	}
	
	public void incrementOccurenceCount(String recogniser,String entityType)
	{
		if(!recognisers.contains(recogniser))
		{
			recognisers.add(recogniser);
			count+=1;
		}
		if(entityTypes.containsKey(entityType))
		{
			Integer val = entityTypes.get(entityType);
			entityTypes.put(entityType, val+1);
		}
		else
		{
			entityTypes.put(entityType, 1);
		}
		
	}
	public boolean isGrobidQuantitiesTagged()
	{
		for(String recogniser : recognisers)
		{
			if(recogniser.equals(CompositeNERecogniser.NER_GROBIDQUANTITIES_NAME))
			{
				return true;
			}
		}
		return false;
	}
	
	
	public String getTypeByMajorityVoting()
	{
		Integer max =0;
		String majorityType="";
		for(Entry<String,Integer> entry: entityTypes.entrySet())
		{
			if(entry.getValue()>=max)
			{
				max=entry.getValue();
				majorityType = entry.getKey();
			}
		}
		return majorityType;
	}
}
