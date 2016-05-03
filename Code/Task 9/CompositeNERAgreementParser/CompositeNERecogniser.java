package org.apache.tika.parser.CompositeNERAgreementParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ner.NERecogniser;
import org.apache.tika.parser.ner.NamedEntityParser;
import org.apache.tika.parser.ner.corenlp.CoreNLPNERecogniser;
import org.apache.tika.parser.ner.grobidquantities.GrobidQuantitiesNERecogniser;
import org.apache.tika.parser.ner.nltk.NLTKNERecogniser;
import org.apache.tika.parser.ner.opennlp.OpenNLPNERecogniser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class CompositeNERecogniser implements NERecogniser {

	private final static String NER_NLTK_NAME = "NLTK";
	private final static String NER_CORENLP_NAME = "CORENLP";
	private final static String NER_OPENNLP_NAME = "OPENNLP";
	public final static String NER_GROBIDQUANTITIES_NAME = "GROBIDQUANTITIES";
	
	private final static int neededAgreement = 2;
	private NamedEntityParser NLTKParser;
	private NamedEntityParser CoreNLPParser;
	private NamedEntityParser OpenNLPParser;
	private NamedEntityParser GrobidQuantitiesParser;
	

	private static final Logger LOG = LoggerFactory
			.getLogger(CompositeNERecogniser.class);

	public static final Set<String> ENTITY_TYPES = new HashSet<String>() {
		{
			add("PERSON");
			add("NAMES");
			add("DATE");
			add("ORGANIZATION");
			add("LOCATION");
			add("MEASUREMENTS");
		}
	};

	public CompositeNERecogniser() {

		NLTKParser = new NamedEntityParser();
		CoreNLPParser = new NamedEntityParser();
		OpenNLPParser = new NamedEntityParser();
		GrobidQuantitiesParser = new NamedEntityParser();
		
		InputStream stream = new ByteArrayInputStream(
				"".getBytes(StandardCharsets.UTF_8));
		ContentHandler handler = new BodyContentHandler(-1);
		Metadata metadata = new Metadata();
		/*
		 * This looks a little(very) weird. However By doing the following we
		 * are actually initializing all the parsers only once Another way to do
		 * that could be setting properties in their respective methods but then
		 * System.setProperty would be called for each file. Thats also not a
		 * big deal.
		 */
		try {
			System.setProperty(NamedEntityParser.SYS_PROP_NER_IMPL,
					NLTKNERecogniser.class.getName());
			NLTKParser.parse(stream, handler, metadata);
			System.setProperty(NamedEntityParser.SYS_PROP_NER_IMPL,
					CoreNLPNERecogniser.class.getName());
			CoreNLPParser.parse(stream, handler, metadata);
			System.setProperty(NamedEntityParser.SYS_PROP_NER_IMPL,
					OpenNLPNERecogniser.class.getName());
			OpenNLPParser.parse(stream, handler, metadata);
			System.setProperty(NamedEntityParser.SYS_PROP_NER_IMPL,
					GrobidQuantitiesNERecogniser.class.getName());
			GrobidQuantitiesParser.parse(stream, handler, metadata);

		} catch (IOException | SAXException | TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Metadata NERRunGrobidQuantities(String text)
			throws FileNotFoundException {
		System.out.println("Running Grobid Quantities");
		InputStream stream = new ByteArrayInputStream(
				text.getBytes(StandardCharsets.UTF_8));
		Metadata md = new Metadata();
		BodyContentHandler handler = new BodyContentHandler(-1);

		try {
			try {
				GrobidQuantitiesParser.parse(stream, handler, md);
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
		System.out.println(md);
		return md;
	}

	private Metadata NERRunOpenNlp(String text) throws FileNotFoundException {
		System.out.println("Running Open NLP");
		InputStream stream = new ByteArrayInputStream(
				text.getBytes(StandardCharsets.UTF_8));
		Metadata md = new Metadata();
		BodyContentHandler handler = new BodyContentHandler(-1);

		try {
			try {
				OpenNLPParser.parse(stream, handler, md);
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
		//System.out.println(md);
		return md;

	}

	private Metadata NERRunCoreNlp(String text) throws FileNotFoundException {
		System.out.println("Running Core NLP");
		InputStream stream = new ByteArrayInputStream(
				text.getBytes(StandardCharsets.UTF_8));

		Metadata md = new Metadata();

		BodyContentHandler handler = new BodyContentHandler(-1);

		try {
			try {
				CoreNLPParser.parse(stream, handler, md);
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
		//System.out.println(md);
		return md;
	}

	private Metadata NERRunNLTK(String text) throws FileNotFoundException {
		System.out.println("Running NLTK");
		InputStream stream = new ByteArrayInputStream(
				text.getBytes(StandardCharsets.UTF_8));

		Metadata md = new Metadata();
		BodyContentHandler handler = new BodyContentHandler(-1);

		try {
			try {
				NLTKParser.parse(stream, handler, md);
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
		//System.out.println(md);
		return md;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public Set<String> getEntityTypes() {
		// TODO Auto-generated method stub
		return ENTITY_TYPES;
	}
	
	

	

	@Override
	public Map<String, Set<String>> recognise(String text) {
		Hashtable<String, TaggedWord> taggedWords = new Hashtable<String, TaggedWord>();
		 int sumNLTK=0;
		 int sumOpenNLP=0;
		 int sumCoreNLP=0;
		 int sumGrobidQuantities=0;
		
		Map<String, Set<String>> set = new HashMap<String, Set<String>>();
		try {

			Metadata NLTKMetadata = NERRunNLTK(text);
			System.out.println(NLTKMetadata);
			Metadata OpenNLPMetadata = NERRunOpenNlp(text);
			System.out.println(OpenNLPMetadata);
			Metadata CoreNLPdata = NERRunCoreNlp(text);
			Metadata GrobidQuantitiesData=NERRunGrobidQuantities(text);


			// Looks like a cubic complexity but not really under the hood
			for (String entityTypeKey : ENTITY_TYPES) {
				ArrayList<ArrayList<Pair>> pairsForAllNERs = new ArrayList<ArrayList<Pair>>();
				ArrayList<Pair> NLTKEntities = getPairs(NLTKMetadata,
						entityTypeKey, NER_NLTK_NAME);
				ArrayList<Pair> openNLPEntities = getPairs(OpenNLPMetadata,
						entityTypeKey, NER_OPENNLP_NAME);
				ArrayList<Pair> coreNLPEntities = getPairs(CoreNLPdata,
						entityTypeKey, NER_CORENLP_NAME);
				ArrayList<Pair>GrodbidQuantitiesEntities=getPairs(GrobidQuantitiesData,entityTypeKey,NER_GROBIDQUANTITIES_NAME);
				
				sumNLTK+=NLTKEntities.size();
				sumOpenNLP+=openNLPEntities.size();
				sumCoreNLP+=coreNLPEntities.size();
				sumGrobidQuantities+=GrodbidQuantitiesEntities.size();
				
				pairsForAllNERs.add(NLTKEntities);
				pairsForAllNERs.add(openNLPEntities);
				pairsForAllNERs.add(coreNLPEntities);
				pairsForAllNERs.add(GrodbidQuantitiesEntities);

				for (ArrayList<Pair> pairList : pairsForAllNERs) {
					for (Pair pair : pairList) {
						String word = pair.getWord();
						// We check whether it is already tagged
						TaggedWord taggedWord = null;

						if (taggedWords.containsKey(word)) {
							taggedWord = taggedWords.get(word);

						} else {
							taggedWord = new TaggedWord(pair.getWord());
						}

						taggedWord.incrementOccurenceCount(
								pair.getRecogniser(), entityTypeKey);
						taggedWords.put(word, taggedWord);
					}
				}

			}
			//printTaggedWords(taggedWords);

			Set<String> keys = taggedWords.keySet();
			for (String key : keys) {
				Set<String> items = new HashSet<String>();
				TaggedWord taggedWord = taggedWords.get(key);
				if (taggedWord.getCount() >= neededAgreement || taggedWord.isGrobidQuantitiesTagged() )
				{
					
					if (set.containsKey(taggedWord.getTypeByMajorityVoting())) {
						items = set.get(taggedWord.getTypeByMajorityVoting());
						items.add(key);
					} else {
						items = new HashSet<String>();
						items.add(key);
					}
				set.put(taggedWord.getTypeByMajorityVoting(), items);
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//Add the sum for each Recogniser
		JSONObject jsonObj = new JSONObject();
		jsonObj.append("SUM_NLTK", sumNLTK);
		jsonObj.append("SUM_OPENNLP", sumOpenNLP);
		jsonObj.append("SUM_CORENLP", sumCoreNLP);
		jsonObj.append("SUM_GROBIDQUANTITIES",sumGrobidQuantities );
		Set<String>sums = new HashSet<String>();
		sums.add(jsonObj.toString());
		set.put("SUMS",sums);
		
		return set;
	}

	public static void printTaggedWords(
			Hashtable<String, TaggedWord> taggedWords) {
		System.out.println("///////////////////////////////////");
		Set<String> keys = taggedWords.keySet();
		for (String key : keys) {
			TaggedWord taggedWord = taggedWords.get(key);
			System.out.println(key + " , "
					+ String.valueOf(taggedWord.getCount()));
		}
	}

	public static ArrayList<Pair> getPairs(Metadata metadata, String key,
			String recogniser) {
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		key = "NER_" + key;
		String[] entities = metadata.getValues(key);
		for (String entity : entities) {
			pairs.add(new Pair(entity.trim().replace(",", ""), recogniser));
		}
		return pairs;
	}

}
