package org.apache.tika.parser.ner.grobidquantities;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ner.NERecogniser;
import org.apache.tika.parser.ner.NamedEntityParser;
import org.apache.tika.parser.ner.nltk.NLTKNERecogniser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Iterator;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



public class GrobidQuantitiesNERecogniser implements NERecogniser {

	private static final Logger LOG = LoggerFactory.getLogger(GrobidQuantitiesNERecogniser.class);
	private static boolean available=false;
	private static final String DEFAULT_GROBID_QUANTITIES_HOST ="http://localhost:8080";
	private String host="";
	 public static final Set<String> ENTITY_TYPES = new HashSet<String>(){{
	        add("UNITS");
	        add("MEASUREMENTS");
	    }};
	
	
	
	
	public GrobidQuantitiesNERecogniser()
	{
		try
		{
			host = readRestUrlFromProperties();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		if(host == null || host.equals(""))
		{
			host= DEFAULT_GROBID_QUANTITIES_HOST;
			LOG.info("No properties selected, Host URL is set as " + DEFAULT_GROBID_QUANTITIES_HOST);
		}
		
	
		try
		{
			Response response = WebClient.create(host).accept(MediaType.TEXT_HTML).get();
			int responseCode = response.getStatus();
			if(responseCode==200)
			{
				available=true;
			}
			else
			{
				LOG.info("GrobidQuantities Server is not accessible");
				System.out.println("GrobidQuantities Server is not accessible");
				available=false;
			}
		}
		
		catch(Exception e)
		{
			available=false;
			LOG.debug(e.getMessage(), e);
		}
		
	}
	
    private static String readRestUrlFromProperties() throws IOException {
        Properties GrobidQuantitiesProperties = new Properties();
        GrobidQuantitiesProperties.load(GrobidQuantitiesNERecogniser.class
                .getResourceAsStream("grobidquantitiesNERecogniser.properties"));

        return GrobidQuantitiesProperties.getProperty("GrobidQuantities.server.url");
    	
    }
	
	@Override
	public boolean isAvailable() {
		return available;
	}

	@Override
	public Set<String> getEntityTypes() {
		return ENTITY_TYPES;
	}

	@Override
	public Map<String, Set<String>> recognise(String text) {
		
		Set<String> measurements= new HashSet<String>();
		Set<String> units = new HashSet<String>();
		Map<String,Set<String>> entitySets = new HashMap();
		try
		{
			String request = host + "/processQuantityText";
			Response response = WebClient.create(request).accept(MediaType.APPLICATION_JSON).post("text=" + text);
			int responseCode= response.getStatus();
			if(responseCode==200)
			{
				String result = response.readEntity(String.class);
				JSONParser parser = new JSONParser();
				JSONObject jObj =(JSONObject) parser.parse(result);
				JSONArray jsonArray = (JSONArray)jObj.get("measurements");
				java.util.Iterator iter=jsonArray.iterator();
				while(iter.hasNext())
				{
					String rawValue="N/A";
					String rawUnit="N/A";
					String type ="N/A";
					String combinedMeasurement="";
					JSONObject measurementObj = (JSONObject) iter.next();
					if(measurementObj.containsKey("quantity"))
					{
						
						
						JSONObject quantityObj =(JSONObject) measurementObj.get("quantity");
						if(quantityObj.containsKey("rawValue"))
						{
							rawValue=(String) quantityObj.get("rawValue");
							if(quantityObj.containsKey("parsedValue"))
							{
								rawValue=String.valueOf(quantityObj.get("parsedValue"));
							}
						}
						if(quantityObj.containsKey("rawUnit"))
						{	JSONObject rawUnitObj = (JSONObject) quantityObj.get("rawUnit");
							rawUnit = (String) rawUnitObj.get("name");
							
						}
						if(quantityObj.containsKey("type"))
						{
							 type=(String)quantityObj.get("type");
						}
						
						if(!rawUnit.equals(""))
						{
							//Then we have a complete measurement with units
							if(!rawValue.equals(""))
							{
								combinedMeasurement= rawValue+ ";" + rawUnit + ";" + type;
								measurements.add(combinedMeasurement);
							}
							units.add(rawUnit);
						}
						
						
						
					}
					
					
				}
				entitySets.put("MEASUREMENTS", measurements);
				//entitySets.put("UNITS", units);
				return entitySets;
				
			}
			
		}
		catch(Exception e)
		{
			LOG.error(e.toString());
		}
		return null;
	}
	/*
	public static void main(String[] args) throws FileNotFoundException
	{
		/*
		// TODO Auto-generated method stub
		String configFile = "tika-config.xml";
		 System.setProperty(NamedEntityParser.SYS_PROP_NER_IMPL,
	                GrobidQuantitiesNERecogniser.class.getName());
		 TikaConfig config=null;
		try {
			config = new TikaConfig(configFile);
		} catch (TikaException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	        Tika tika = new Tika(config);

	        
	           Metadata md = new Metadata();
	        NamedEntityParser parser = new NamedEntityParser();
	        BodyContentHandler handler = new BodyContentHandler();
	        File file = new File("check2.txt");
	        InputStream stream =TikaInputStream.get(file,md);
	        try {
				try {
					parser.parse(stream, handler, md);
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TikaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        HashSet<String> set = new HashSet<String>();
	        
	}*/
	
	

}
