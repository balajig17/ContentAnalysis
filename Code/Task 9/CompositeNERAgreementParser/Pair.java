package org.apache.tika.parser.CompositeNERAgreementParser;

public class Pair {
	private String recogniser;
	private String word;
	
	
	public Pair(String word,String recogniser)
	{
		this.word=word;
		this.recogniser=recogniser;
	}
	public Pair()
	{
		
	}
	
	public String getRecogniser() {
		return recogniser;
	}
	public void setRecogniser(String recogniser) {
		this.recogniser = recogniser;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}

}
