import java.util.*;


public class CountedWord { //implements Comparable{
	public String word;
	public int count;
	public float percent;
	final int MINIMUM_WORD_LENGTH = 4;

	CountedWord(String newWord) {
		this(newWord, 1, 0f);
	}
	
	CountedWord (String newWord, int newCount, float newPercent) {
		word = newWord;
		count = newCount;
		percent = newPercent;
	}
		
	public void calcPercent(int totalWords) {
		percent = ((float)count)/totalWords * 100;
	}
	
	public boolean hasCapital() {
		boolean hasCapital = false;
		if(upperCaseIndex() >= 0)
			hasCapital = true;
		return hasCapital;
	}
	
	public boolean isShort() {
		boolean isShort = false;
		if(word.length() < MINIMUM_WORD_LENGTH)
			isShort = true;
		return isShort;
	}
	
	public int upperCaseIndex() {        
	    for(int i = 0; i < word.length(); i++) {
	        if(Character.isUpperCase(word.charAt(i))) {
	            return i;
	        }
	    }
	    return -1;
	}
	
	public void cleanWord() {
		word = word.replaceAll("[^a-zA-Z ]", "");
	}
	
	public boolean isStopWord() {
		boolean isStopWordBool = false;
		final String[] STOP_WORDS = {"a", "able", "about", "across", "after", "all", "almost",
				"also", "am", "among", "an", "and", "any", "are", "as", "at", "be", "because",
				"been", "but", "by", "can", "cannot", "could", "dear", "did", "do", "does",
				"either", "else", "ever", "every", "for", "from", "get", "got", "had", "has",
				"have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in",
				"into", "is", "it", "its", "just", "least", "let", "like", "likely", "may", "me",
				"might", "most", "must", "my", "neither", "no", "nor", "not", "of", "off", "often",
				"on", "only", "or", "other", "our", "own", "pp", "rather", "said", "say", "says", "she",
				"should", "since", "so", "some", "than", "that", "the", "their", "them", "then",
				"there", "these", "they", "this", "tis", "to", "too", "twas", "us", "wants", "was",
				"we", "were", "what", "when", "where", "which", "while", "who", "whom", "why",
				"will", "with", "would", "yet", "you", "your"};
		
		final String[] JUNK_WORDS = {"ue", "uea", "ueueue", "uec", "px", "title", "url", "cite", "webu800", "urlu800", "web", 
				"accessdate", "publisher", "date", "january","february","march","april","may","june","july",
				"august","september","november","december","year","month","day"}; //due to misc reasons, my personal list
		if(!word.contains("a") && !word.contains("e") && !word.contains("i") && !word.contains("o") && !word.contains("u")) {
			if(!word.contains("IBM"))
				isStopWordBool = true;
		}
		if(word.contains("ueue"))
			isStopWordBool = true;
		
		for(int i = 0; i < STOP_WORDS.length; i++) {
			if(STOP_WORDS[i].equalsIgnoreCase(word)) {
				//System.out.println(word);
				isStopWordBool = true;
			}
		}
		
		for(int i = 0; i < JUNK_WORDS.length; i++) {
			if(JUNK_WORDS[i].equalsIgnoreCase(word)) {
				//System.out.println(word);
				isStopWordBool = true;
			}
		}
		
		if(word.length() < 2) {
			isStopWordBool = true;
		}
		return isStopWordBool;
	}
	
	public static Comparator<CountedWord> alphaComparator = new Comparator<CountedWord>() {
	    public int compare(CountedWord word1, CountedWord word2) {
	    	return word1.word.compareToIgnoreCase(word2.word);
	    }
	};
	
	public static Comparator<CountedWord> countComparator = new Comparator<CountedWord>() {
	    public int compare(CountedWord word1, CountedWord word2) {
	    	return word2.count - word1.count;
	    }
	};
}

