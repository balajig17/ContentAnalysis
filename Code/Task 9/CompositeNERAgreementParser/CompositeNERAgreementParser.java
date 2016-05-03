package org.apache.tika.parser.CompositeNERAgreementParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
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
import org.apache.tika.parser.ner.NamedEntityParser;
import org.apache.tika.parser.ner.grobidquantities.GrobidQuantitiesNERecogniser;
import org.apache.tika.parser.ner.nltk.NLTKNERecogniser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class CompositeNERAgreementParser extends AbstractParser {

	private static final Logger LOG = LoggerFactory
			.getLogger(CompositeNERAgreementParser.class);
	private static final NamedEntityParser nameEntityParser = new NamedEntityParser();

	@Override
	public Set<MediaType> getSupportedTypes(ParseContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parse(InputStream stream, ContentHandler handler,
			Metadata metadata, ParseContext context) throws IOException,
			SAXException, TikaException {
		System.setProperty(NamedEntityParser.SYS_PROP_NER_IMPL,
				CompositeNERecogniser.class.getName());

		try {
			try {
				nameEntityParser.parse(stream, handler, metadata);
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
		System.out.println(metadata);

	}

	public void parse(String str, Metadata metadata) throws IOException {
		System.setProperty(NamedEntityParser.SYS_PROP_NER_IMPL,
				CompositeNERecogniser.class.getName());
		BodyContentHandler handler = new BodyContentHandler();
		InputStream stream = new ByteArrayInputStream(
				str.getBytes(StandardCharsets.UTF_8));
		try {
			try {
				nameEntityParser.parse(stream, handler, metadata);
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
		System.out.println(metadata);

	}
	/*
	public static void main(String[] args) throws FileNotFoundException {
		Metadata md = new Metadata();
		File file = new File("check.txt");
		BodyContentHandler handler = new BodyContentHandler();
		InputStream stream = TikaInputStream.get(file, md);
		String content = null;
		try {
			content = IOUtils.toString(stream, "UTF-8");
			parse(content, md);
			// parse(content,md);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/

}
