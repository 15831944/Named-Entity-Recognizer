import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class NLPUtils {
	

	
	/*
	 * Reads each bag, extracts the words (ignoring counts) and collects a unique list of all words found
	 */
	public static ArrayList<String> uniqueListFromBags(ArrayList<BagOfWords> allBags, int minCount, float minPercent) {
		ArrayList<String> allWords = new ArrayList<String>();
		for(BagOfWords bag : allBags) {
			bag.thresholdTrim(minCount, minPercent);
			ArrayList<String> bagWords = bag.toStringArray();
			//System.out.println(bagWords);
			for(String newWord : bagWords) {
				if(!allWords.contains(newWord)) {
					allWords.add(newWord);
				}
			}
		}
		Collections.sort(allWords);
		//System.out.println("Unique list " + allWords);
		return allWords;
	}
	
	public static ArrayList<int[]> bags2vectors(ArrayList<BagOfWords> allBags, ArrayList<String> vocabList, int minCount, float minPercent) {
		ArrayList<int[]> dataMatrix = new ArrayList<int[]>();
		int numColumns = vocabList.size();
		for(BagOfWords bag : allBags) {
			bag.thresholdTrim(minCount, minPercent);
			ArrayList<String> wordList = bag.toStringArray();
			int[] dataLine = new int[numColumns];
			//System.out.println(file + " " + fileWords);
			for(String newWord : wordList) {
				//System.out.println(newWord);
				int wordIndex = vocabList.indexOf(newWord);
				if(wordIndex > 0)
					dataLine[wordIndex] = 1;
			}
			dataMatrix.add(dataLine);
		}
		
		//Display contents
		for(int i = 0; i < dataMatrix.size(); i++) {
			//System.out.println(i + " " + trainingData.get(i)[numColumns]);
		}
		return dataMatrix;
	}
	
}//end of class
