import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;

public class NLPdriver {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {	
		ArrayList<String> requiredSoftwareNames = new ArrayList<String>(); //Purpose of the NLP module
		
		
		/////////////////////////////////////////////////////////////////////////get raw words from website///////////////////////////
		final String ICS_462 = "http://www2.hawaii.edu/~chin/462/";
		//final String ICS_332 = "http://navet.ics.hawaii.edu/~casanova/courses/ics332_spring13/";
		final String ICS_321 = "http://www2.hawaii.edu/~lipyeow/ics321/2013fall/";
		
		String websiteRawTextFileName = "websiteExtractedRawText.txt";
		File websiteTextFile = new File(websiteRawTextFileName);
		String possibleSoftwareNames = "classWebsiteCandidateWords.txt";
		
		String targetClassPage = ICS_462;
		boolean forceWebCrawl = true;
		//forceWebCrawl = true;
		if(!FileUtils.isFileThere(websiteRawTextFileName) || forceWebCrawl) {
			System.out.println("Reading text from: " + targetClassPage);
			websiteExtractor we = new websiteExtractor();
			websiteTextFile = we.extract(targetClassPage, websiteRawTextFileName);				
		}
		else {
			System.out.println("Website extraction already exists.");
		}
		
		//recognize names of software within the text
		
		//get candidate words from raw text//////////////////////////////
		System.out.println("Extract candidate words from raw word list");
		BagOfWords candidateBag = new BagOfWords(websiteTextFile);
		FileUtils.writeToFile(candidateBag.toStringArray(), possibleSoftwareNames);
		
				
		////////////////////////////////////////////////////////////////////////make a bag/bagFile for each candidate phrase////////////////
		System.out.println("\nOffline wikipedia search for candidate phrases.");
		int webCountThreshold = 1;
		boolean webForceOverwrite = false;
		String candidateDirectory = "data/unclassified/";

		if(forceWebCrawl) {
			File dir = new File(candidateDirectory);
			for(File file: dir.listFiles()) file.delete();
		}
		//NLPUtils.searchBagStore(possibleSoftwareNames, "data/unclassified", "badWebTerms.txt", "goodWebTerms.txt", webCountThreshold, webForceOverwrite);
		ArrayList<String> candidateWords = candidateBag.toStringArray();
		for(int i = 0 ; i < candidateWords.size(); i++) {
			System.out.println("\nSearching candidate: " + candidateWords.get(i) + " " + i + "/" + (candidateWords.size() - 1));
			Timer.start();
			BagOfWords tempBag = new BagOfWords(candidateWords.get(i), "offline");
			Timer.stop();
			if(tempBag.size() > 0) {
				FileUtils.writeToFile(tempBag, candidateDirectory + candidateWords.get(i) + ".txt");
			}
		}		
		
		////////////////////////////////////////////////////////////////////////build Ground Truth Model from list of software and nouns/////////
		int countThreshold = 1;
		boolean forceOverwrite = false;
		String nounDirectory = "data/nouns/";
		String softwareNameDirectory = "data/softwares/";
		String exampleNounListFileName = "nounList.txt";
		String exampleSoftwareListFileName = "softwareNames.txt";
		
		//getNoun articles and bags
		System.out.println("\nOffline wikipedia search for confirmed nouns.");
		//NLPUtils.searchBagStore("nounList.txt", "data/nouns", "badSearchTerms.txt", "goodSearchTerms.txt", countThreshold, forceOverwrite);
		ArrayList<String> nouns = FileUtils.readDataFile(exampleNounListFileName);
		for(int i = 0 ; i < nouns.size(); i++) {
			System.out.println("\nSearching noun: " + nouns.get(i) + " " + i + "/" + (nouns.size() - 1));
			Timer.start();
			BagOfWords tempBag = new BagOfWords(nouns.get(i), "offline");
			Timer.stop();
			if(tempBag.size() > 0)
				FileUtils.writeToFile(tempBag, nounDirectory + nouns.get(i) + ".txt");
		}
		
		//get software articles and bags
		System.out.println("\nOffline wikipedia search for confirmed software names.");
		//NLPUtils.searchBagStore("softwareNames.txt", "data/softwares", "badSoftSearchTerms.txt", "goodSoftSearchTerms.txt", countThreshold, forceOverwrite);	
		ArrayList<String> softwareNames = FileUtils.readDataFile(exampleSoftwareListFileName);
		for(int i = 0 ; i < softwareNames.size(); i++) {
			System.out.println("\nSearching software:" + softwareNames.get(i) + " " + i + "/" + (softwareNames.size() - 1));
			Timer.start();
			BagOfWords tempBag = new BagOfWords(softwareNames.get(i), "offline");
			Timer.stop();
			if(tempBag.size() > 0)
				FileUtils.writeToFile(tempBag, softwareNameDirectory + softwareNames.get(i) + ".txt");
		}
		
		
		//build key vocab list //TODO possibly add thresholding here
		int minCountVocab = 2;
		float minPercentVocab = 0.2f;
		System.out.println();
		ArrayList<BagOfWords> softwareBags 	= FileUtils.readBagsInDirectory(softwareNameDirectory);
		ArrayList<BagOfWords> nounBags 		= FileUtils.readBagsInDirectory(nounDirectory);
		ArrayList<BagOfWords> candidateBags = FileUtils.readBagsInDirectory(candidateDirectory);
		
		//make list of candidate words used so we can match up indexes with words for interpretation later on
		ArrayList<String> candidateWordList = FileUtils.readFileNames(candidateDirectory);
		FileUtils.writeToFile(candidateWordList, "candidateWordList.txt");
		
		ArrayList<String> uniqueSoftwareTerms 	= NLPUtils.uniqueListFromBags(softwareBags, minCountVocab, minPercentVocab);
		//ArrayList<String> uniqueNounTerms 		= NLPUtils.uniqueListFromBags(nounBags, minCountVocab, minPercentVocab);
		//ArrayList<String> uniqueCandidateTerms 	= NLPUtils.uniqueListFromBags(candidateBags, minCountVocab, minPercentVocab);
		
		ArrayList<String> vocabList = uniqueSoftwareTerms; //only looking at presence or absence of these terms
		System.out.println("Writing out vocabulary list");
		//System.out.println("All words in relevant vocabulary: " + vocabList);
		FileUtils.writeToFile(vocabList, "vocabList.txt");
		
		//build master list
		int minCountData = 2;
		float minPercentData = 0.2f;
				
		ArrayList<int[]> positiveData = new ArrayList<int[]>();
		ArrayList<int[]> negativeData = new ArrayList<int[]>();
		ArrayList<int[]> unclassifiedData = new ArrayList<int[]>();

		positiveData	 = NLPUtils.bags2vectors(softwareBags, vocabList, minCountData, minPercentData);
		negativeData 	 = NLPUtils.bags2vectors(nounBags, vocabList, minCountData, minPercentData);
		unclassifiedData = NLPUtils.bags2vectors(candidateBags, vocabList, minCountData, minPercentData);

		FileUtils.writeDataFile(positiveData, "positiveSampleData.txt");
		FileUtils.writeDataFile(negativeData, "negativeSampleData.txt");
		FileUtils.writeDataFile(unclassifiedData, "unclassifiedCandidateData.txt");
		
		/* this may be depreciated. Using ML to generate a ground truth model
		//load ground truth model for our topic///////////////////////////
		System.out.println("Loading list of known words related to Software.");
		BagOfWords knownSoftwareWords = new BagOfWords("softwareWordList.txt", "model");
		*/
		
		
		//matlab machine learning
		try {
			requiredSoftwareNames = MatlabUtils.predictSoftwareNames("verbose");
		} catch (MatlabConnectionException e) {
			e.printStackTrace();
		} catch (MatlabInvocationException e) {
			e.printStackTrace();
		}
				
		//output report / list of quality words
		System.out.println();
		System.out.println("This class requires the following software: ");
		for(int i = 0; i < requiredSoftwareNames.size(); i++) {
			System.out.println(requiredSoftwareNames.get(i));
		}
		
		//Find and download the installer for these programs.
		//Add code for yes/no from user, or way to get out/skip if we are trying a bad word. 
		for(int i = 0; i < requiredSoftwareNames.size(); i++) {
			//downloadWebpage("http://www.bing.com/search?q=firefox");
			ArrayList<URL> searchResults = WebSearchUtils.searchBing(requiredSoftwareNames.get(i)); //"dropbox"); //mse");
			if(searchResults.size() > 0) {
				WebSearchUtils.findInstaller(searchResults.get(0));
			}
			else {
				System.out.println("Error: Web search found no links.");
			}
		}

		System.out.println("Done.");
	}
	

}
