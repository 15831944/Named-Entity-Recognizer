import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class BagOfWords {

	ArrayList<CountedWord> bagOfWords = new ArrayList<CountedWord>();

	BagOfWords() {
		
	}
	
	/* This constructor is different in 2 ways. Watch it.
	 * 1. This takes a File. This will change. Currently testing a data dump of a class website to save time. Will like to Website Extractor later
	 * 2. This version only grabs the capitalized words expecting they are candidates for software names
	 * Note: Should add phrase capturing for consecutive capitalized words. Then permute on them.
	 */
	BagOfWords (File rawText) {
		ArrayList<CountedWord> wordList = new ArrayList<CountedWord>();
		try {
			FileReader fr = new FileReader(rawText);
			BufferedReader br = new BufferedReader(fr);
			String line;
			String allText = "";
			while((line = br.readLine()) != null) {
				//parse into words
				allText = allText.concat(line);
				//System.out.println(allText);
			}//end of while
			String[] words = allText.split("\\s+");
			for(int i = 0; i < words.length; i++) {
				CountedWord testWord = new CountedWord(words[i]); 
				testWord.cleanWord();
				ArrayList<CountedWord> conseqCapWords = new ArrayList<CountedWord>();
				while(testWord.hasCapital() && !testWord.isStopWord() && !testWord.isShort()) {
					conseqCapWords.add(testWord);
					i++;
					if(i < words.length) {
						testWord = new CountedWord(words[i]); 
						testWord.cleanWord();
					}
					else {
						//System.out.println(i);
						break;
					}
				}
				if(conseqCapWords.size() > 0) {
					i--; //avoid double increment from while and for. 
					addCapPhrases(conseqCapWords, wordList);
				}
			}
			
			br.close();
			fr.close();
		}//end of try
		catch (Exception e) {
			System.out.println("Error in BagOfWords (File rawText): " + e.toString() );
		}
		sortAlpha(wordList);
		//display(wordList);
		bagOfWords = getWordCounts(wordList);
		sortCount(bagOfWords);
		//display(bagOfWords);
	}
	
	/* This version is for building a bag of words from wikipedia, softpedia or some source so we can analyze if it's talking about a piece of software or not.
	 * 
	 */
	BagOfWords(String searchTerm) {
		ArrayList<CountedWord> allWords = new ArrayList<CountedWord>();
		try {
			final URI WIKIPEDIA_DOMAIN = new URI("http://en.wikipedia.org/wiki/");
			//final URI WIKIPEDIA_SEARCH_DOMAIN = new URI("http://en.wikipedia.org/wiki/Special:Search/");

			searchTerm = searchTerm.replace(" ", "_");
			URI searchURIStub = new URI(searchTerm);
			URI testURI = WIKIPEDIA_DOMAIN.resolve(searchURIStub);
			//System.out.println("\nBuilding bag from article: " + testURI.toString());
			
			Document doc = Jsoup.connect(testURI.toString()).get();
			//System.out.println(doc.text());
			String[] words = doc.text().split("\\s+");
			for(int i = 0; i < words.length; i++) {
				CountedWord testWord = new CountedWord(words[i]); 
				testWord.cleanWord();
				if(!testWord.isStopWord() && testWord.word.length() > 0) {
					//System.out.println(testWord); //making numbers blank. Should be OK though, dont need them
					allWords.add(testWord);
				}
			}
			
			sortAlpha(allWords);
			bagOfWords = getWordCounts(allWords);
			sortCount(bagOfWords);
			//display(bagOfWords);
		} 
		catch (URISyntaxException e) {
			e.printStackTrace();
		} 
		catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/*
	 * Alternate constructor for manually building a model from a word list
	 */
	BagOfWords(String string, String optionString, int countThreshold) {
		int optionInt = -1;
		int MODEL = 1;
		int OFFLINE = 2;
		
		if(optionString.equalsIgnoreCase("model"))
			optionInt = MODEL;
		if(optionString.equalsIgnoreCase("offline"))
			optionInt = OFFLINE;
		
		if(optionInt == MODEL) {
			String modelWordsFileName = string;
			try {
				FileReader fr = new FileReader(new File(modelWordsFileName));
				BufferedReader br = new BufferedReader(fr);
				String line;
				
				while((line = br.readLine()) != null) {
					bagOfWords.add(new CountedWord(line, 99, 99));
				}//end of while
				
				br.close();
				fr.close();
				
			}//end of try
			catch (Exception e) {
				System.out.println("Error in BagOfWords Model Maker " + e.toString() );
			}
		}
		
		if(optionInt == OFFLINE) {
			ArrayList<CountedWord> allWords = new ArrayList<CountedWord>();
			String searchTerm = string;
			String rawText = null;
			String[] words = null;
			String badSearchTermsFileName = "badSearchTerms.txt";
			ArrayList<String> badSearchTerms = new ArrayList<String>();
			
			//check if we've already searched for this word and failed
			if(FileUtils.isFileThere(badSearchTermsFileName))
				badSearchTerms = FileUtils.readDataFile(badSearchTermsFileName);
			if(badSearchTerms.size() == 0 || !badSearchTerms.contains(searchTerm)) {
				//get the words from the wikipedia article. 
				try {
					String outputFileFullPath = "data/rawText/" + searchTerm + "_raw.txt";
					
					//try to read an archived version first. My DB searching is pretty time intensive..
					if(FileUtils.isFileThere(outputFileFullPath)) {
						rawText = FileUtils.readDataFile(outputFileFullPath).get(0); //TODO confirm size = 0
						System.out.println("Found article text on local drive.");
					}
					else { //do the DB search to build new raw text
						rawText = DatabaseUtils.searchWikiDB(searchTerm);
						if(rawText != null) {
							FileUtils.writeToFile(rawText, outputFileFullPath);
						}
					}
					if(rawText != null)
						words = DatabaseUtils.parseWikiArticle(rawText);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				//try to build a bag from the read and parsed text
				if(words != null) {
					for(int i = 0; i < words.length && !words[i].equalsIgnoreCase("references"); i++) { //ignore reference part of the article
						//System.out.println(words[i]);
						CountedWord testWord = new CountedWord(words[i]); 
						testWord.cleanWord();
						if(!testWord.isStopWord() && testWord.word.length() > 0) {
							//System.out.println(testWord); //making numbers blank. Should be OK though, dont need them
							allWords.add(testWord);
						}
					}
					
					sortAlpha(allWords);
					bagOfWords = getWordCounts(allWords);
					sortCount(bagOfWords);
					thresholdTrim(countThreshold, 0);
					//display(bagOfWords);
				}
				else {
					System.out.println("Error. " + searchTerm + " is not a usable term.");
					if(FileUtils.isFileThere(badSearchTermsFileName)) {
						FileUtils.appendToFile(searchTerm, badSearchTermsFileName);
					} else {
						FileUtils.writeToFile(searchTerm, badSearchTermsFileName);
					}
				}
			} else {
				System.out.println("Automatically skipping: " + searchTerm + " looks like it will not be found.");
			}
		}//end of OFFLINE
	}
	
	BagOfWords(String string, String optionString) {
		this(string, optionString, 1);
	}
	
	public void thresholdTrim(int minCount, float minPercent) {
		for(int i = bagOfWords.size() - 1; bagOfWords.size() > 0 && (bagOfWords.get(i).count < minCount || bagOfWords.get(i).percent < minPercent); i--) {
			bagOfWords.remove(i);
		}
	}
	
	public void display() {
		display(bagOfWords.size());
	}
	
	public void add(String[] entries) {
		String word = entries[0];
		int count = Integer.parseInt(entries[1]);
		float percent = Float.parseFloat(entries[2]);
		bagOfWords.add(new CountedWord(word, count, percent));
	}
	
	public void display(int counter) {
		//float totalPercent = 0;
		for(int i = 0; i < counter && i < bagOfWords.size(); i++) {
			System.out.println("Bag Data: " + bagOfWords.get(i).count + "\t" + bagOfWords.get(i).percent + "\t" + bagOfWords.get(i).word);
			//totalPercent += countedWords.get(i).percent;
		}
		//System.out.println("Total Percent: " + totalPercent);
	}	
	
	public void addCapPhrases(ArrayList<CountedWord> capitalWords, ArrayList<CountedWord> wordList) {
		//System.out.println("-----------------begin Cap Phrases ---------------------");
		//for(int x = 0; x < capitalWords.size(); x++)
		//	System.out.println(capitalWords.get(x).word);
		//System.out.println("------------------------");
		for(int i = 0; i < capitalWords.size(); i++) {
			String capPhrase = "";
			for(int j = i; j < capitalWords.size(); j++) {
				if(!capPhrase.isEmpty())
					capPhrase = capPhrase.concat(" ");
				capPhrase = capPhrase.concat(capitalWords.get(j).word);
				wordList.add(new CountedWord(capPhrase));
				//System.out.println(capPhrase);
			}
		}
		//System.out.println("-----------------end Cap Phrases ---------------------");
	}
	
	public void sortAlpha(ArrayList<CountedWord> countedWords) {
		Collections.sort(countedWords, CountedWord.alphaComparator);
	}
	
	public void sortCount(ArrayList<CountedWord> countedWords) {
		Collections.sort(countedWords, CountedWord.countComparator);
	}
	
	public ArrayList<CountedWord> getWordCounts(ArrayList<CountedWord> wordList) {
		ArrayList<CountedWord> wordCounts = new ArrayList<CountedWord>();
		CountedWord currentWord = null;
		int totalWords = wordList.size();
		for(int i = 0; i < wordList.size( ) - 1; i++) {
			currentWord = new CountedWord(wordList.get(i).word);
			while(i < wordList.size() - 1 && currentWord.word.equalsIgnoreCase(wordList.get(i+1).word)) {
				currentWord.count++;
				wordList.remove(i+1);
			}
			currentWord.calcPercent(totalWords);
			wordCounts.add(currentWord);
		}
		return wordCounts;
	}
	
	public CountedWord get(int index) {
		return bagOfWords.get(index);
	}
	
	public int size() {
		return bagOfWords.size();
	}

	/*
	public void writeToFile(String fileName) {
		File outputFile = new File(fileName);
		try {
			FileWriter fw = new FileWriter(outputFile);
			for(int i = 0; i < bagOfWords.size(); i++) { 
				fw.write(bagOfWords.get(i).count + "\t" + bagOfWords.get(i).percent + "\t" + bagOfWords.get(i).word + "\n");
			}
		}
		catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
		
	}
	*/
	
	public int intersection(BagOfWords otherBag, int minThreshold) {
		int numMatchingWords = 0;
		for(int i = 0; i < bagOfWords.size(); i++) { 
			//System.out.println(otherBag.size());
			for(int j = 0; j < otherBag.size(); j++) {
				//System.out.println(bagOfWords.get(i).word + "\t" + otherBag.get(j).word);
				if(bagOfWords.get(i).word.equalsIgnoreCase(otherBag.get(j).word) && bagOfWords.get(i).count >= minThreshold && otherBag.get(j).count >= minThreshold) {
						System.out.println("Found matching term: " + bagOfWords.get(i).word + " " + bagOfWords.get(i).count + " " + otherBag.get(j).word + " " + otherBag.get(j).count);
						numMatchingWords++;
				}
			}
		}
		System.out.println("Total Matching score: " + numMatchingWords + " Original sizes: " + this.size() + " " + otherBag.size());
		return numMatchingWords;
	}	
		
	public ArrayList<String> toStringArray() {
		ArrayList<String> acceptableWords = new ArrayList<String>();
		
		for(int i = 0; i < bagOfWords.size(); i++) {
			acceptableWords.add(bagOfWords.get(i).word);
		}
		
		return acceptableWords;
	}
		

}//end of class


