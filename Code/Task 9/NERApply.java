import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.nio.file.Paths;

import org.apache.tika.io.IOUtils;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.CompositeNERAgreementParser.CompositeNERAgreementParser;
import org.apache.tika.sax.BodyContentHandler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.BodyContentHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import com.cedarsoftware.util.io.JsonWriter;

import org.xml.sax.SAXException;
import org.apache.tika.sax.BodyContentHandler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;


public class NERVisualizer {

	
	
	
	public static ArrayList<String> filenames = new ArrayList<String>();
	public static ArrayList<String> recogniserNames= new ArrayList<String>();
	public static ArrayList<ArrayList<Integer>> foundItems = new ArrayList<ArrayList<Integer>> ();
	public static ArrayList<Integer> nltkFound= new ArrayList<Integer>();
	public static ArrayList<Integer> coreNLPFound = new ArrayList<Integer>();
	public static ArrayList<Integer> openNLPFound = new ArrayList<Integer>();
	
	private static int executionLimitSeconds = 1550;
	private static String destination = Paths.get(".").toAbsolutePath().normalize().toString();
	private static String startAfter = "";
	private static boolean process = false;
	private static boolean r = false;
	private static Gson gson ;
	private static BodyContentHandler handler;
	private static ExecutorService executor;
	private static CompositeNERAgreementParser parser;
	
	
	
	
	
	
	// If program hangs at some point we can avoid processing the same files.
		public static void resume() throws IOException {
			String filePath = FilenameUtils.concat(destination, "resume.txt");
			System.out.println(filePath);
			File file = new File(filePath);
			if (file.exists()) {
				String str = FileUtils.readFileToString(file);
				if (!str.equals("")) {
					startAfter = str;
					process = false;
				} else {
					process = true;
				}
			} else {
				System.out.println("Starting from beginning");
				process = true;
			    file.createNewFile();
			}
		}

		public static void saveLastFile(String filename) throws FileNotFoundException {
			String filePath = FilenameUtils.concat(destination, "resume.txt");
			PrintWriter writer = new PrintWriter(new FileOutputStream(filePath, false));
			writer.print(filename);
			writer.close();

		}
	
	
	
	
	
	

	
	public static void initialize() throws IOException {
		
		parser = new CompositeNERAgreementParser();
		handler= new BodyContentHandler(-1);
		executor = Executors.newFixedThreadPool(1);
		gson=new Gson();
		recogniserNames.add("NLTK");
	  	recogniserNames.add("OPENNLP");
	  	recogniserNames.add("CORENLP");
		resume();
	}
	
	
	
		
	
	
	
	
	
	
	public static void main(String[] args) throws IOException, SAXException, TikaException 
	{
		  	
		  	Metadata md= new Metadata();	      
		    String root = "/Volumes/My Passport/processes/CombinedNER/input/text";
			destination = "/Volumes/My Passport/processes/CombinedNER/output/text";
			initialize();
			System.out.println("Starting");
			File rootDir = new File(root);
			// Start Parsing
			parseAllFiles(rootDir.listFiles());
	}
	
	
	
	public static void parseAllFiles(File[] files) throws IOException, SAXException, TikaException {
		Arrays.sort(files);
		for (File file : files) {
			if (file.isDirectory()) {
				// Optional if we want to check subdirectories too.
				if (r) {
					parseAllFiles(file.listFiles());
				}
			} else {
				if (process) {
					//System.out.println("Processing " + file.toString());
					if(file.getName().charAt(0)!='.')
					{
						
							saveLastFile(file.getName());
							String jsonRep=parseFile(file);
							writeJsonToFile(jsonRep, file.getName());
						
					}

				} else {
					if (file.getName().equals(startAfter)) {
						System.out.println("Resuming from file" + file.getName());
						process = true;
					}
				}
			}
		}
	}
	
	
	public static void writeJsonToFile(String json, String filename) throws FileNotFoundException {

		String fullPath = FilenameUtils.concat(destination, filename);
		PrintWriter writer = new PrintWriter(new FileOutputStream(fullPath, false));
		json = JsonWriter.formatJson(json);
		writer.write(json);
		writer.close();
	}
	
	public static String preprocessForNER(String content)
	{
		content = content.replace("<", "");
		//System.out.println(content.contains("\n"));
		content=content.replace(">", "");
		content=content.replace("/", "");
		content=content.replace("\\n", "");
		content=content.replace("\\t", "");
		content=content.trim().replaceAll(" +", " ");
		return content;
	}

	public static String parseFile(File file) throws IOException, SAXException, TikaException {

		String fileStr = FileUtils.readFileToString(file);
		// We get the json , we are gonna modify it
		JsonObject jsonObject = (new JsonParser()).parse(fileStr).getAsJsonObject();
		//Extracting the content
		JsonElement contentElement = jsonObject.get("content");
		if (contentElement != null) {

			String content = preprocessForNER(contentElement.toString());
			JsonElement contentTypeElement = jsonObject.get("metadata").getAsJsonObject().get("Content-Type");
			String contentType = contentTypeElement.toString();
			
			InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
			ExecutorService service = Executors.newSingleThreadExecutor();
			Callable<Object> gettingData = new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					// TODO Auto-generated method stub
					Metadata metadata = new Metadata();
					parser.parse(stream, handler, metadata);
					return metadata;
				}

			};

			Future<Object> futureObject = service.submit(gettingData);
			try {
				Metadata metadata = (Metadata) futureObject.get(executionLimitSeconds, TimeUnit.SECONDS);
				JsonElement metadataElement = jsonObject.get("metadata");
				JsonObject jsonMetadataObject = metadataElement.getAsJsonObject();
				String parsingInfoJson= metadata.get("NER_SUMS");
				JsonObject sumsJsonObject= (new JsonParser()).parse(parsingInfoJson).getAsJsonObject();
				int openNLPFound=Integer.parseInt(sumsJsonObject.get("SUM_OPENNLP").getAsString());
				int coreNLPFound =Integer.parseInt(sumsJsonObject.get("SUM_CORENLP").getAsString());
				int NLTKFound = Integer.parseInt(sumsJsonObject.get("SUM_NLTK").getAsString());
				int GrobidFound = Integer.parseInt(sumsJsonObject.get("SUM_GROBIDQUANTITIES").getAsString());
				jsonMetadataObject.addProperty("NLTK_FOUND", String.valueOf(NLTKFound));
				jsonMetadataObject.addProperty("OPENNLP_FOUND", String.valueOf(openNLPFound));
				jsonMetadataObject.addProperty("CORENLP_FOUND", String.valueOf(coreNLPFound));
				jsonMetadataObject.addProperty("GROBIDQUANTITIES_FOUND", String.valueOf(GrobidFound));
				
				ArrayList<String> agreed = new ArrayList<String>();
				String NERMetadataPrefix = "AGREED_";
				Hashtable<String, String[]> hashmap = new Hashtable<String,String[]>();
				int agreedCount=0;
				for(String key : metadata.names())
				{
					if(!key.equals("NER_SUMS"))
					{
						
						String[] values = metadata.getValues(key);
						if(!key.equals("NER_MEASUREMENTS"))
						{
							agreedCount+=values.length;
						}
						String jsonArr= gson.toJson(values);
						
						JsonElement elmnt =  new JsonParser().parse(jsonArr); 
						jsonMetadataObject.add(NERMetadataPrefix+key,elmnt );
						//System.out.println("Hey there");
						
					
					}
				}
				jsonMetadataObject.addProperty("AGREED_FOUND", String.valueOf(agreedCount));
				
				
				 
			

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				System.out.println("This got timed out");
				e.printStackTrace();
			}

		} 
		else 
		{
			return jsonObject.toString();
		
		}
		//String str = jsonObject.toString();
		return jsonObject.toString();
		

	}

}
