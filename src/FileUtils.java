import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileUtils {

	public static ArrayList<String> readDataFile(String filename) {
		return readDataFile(filename, "");
	}
	
	/*
	 * Reads every line of a text file and returns the lines in an ArrayList of strings
	 */
	public static ArrayList<String> readDataFile(String filename, String folderName) {
		ArrayList<String> readText = new ArrayList<String>();
		String relativePathName;
			if(folderName.length() > 0)
				relativePathName = folderName + "/" + filename;
			else
				relativePathName = filename;
		File dataFile = new File(relativePathName);
		try {
			FileReader fr = new FileReader(dataFile);
			BufferedReader br = new BufferedReader(fr);
			
			String line;  
	        while ((line = br.readLine()) != null) {
	            //sb.append(line);
	            //sb.append('\n');
	            readText.add(line);
	        }
	        fr.close();
	        br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return readText;
	}
	
	public static BagOfWords readBagFile(String filename) {
		BagOfWords readBag = new BagOfWords();

		File dataFile = new File(filename);
		try {
			FileReader fr = new FileReader(dataFile);
			BufferedReader br = new BufferedReader(fr);
			
			String line;  
	        while ((line = br.readLine()) != null) {
	        	String[] entries = line.split(",");
	        	readBag.add(entries);
	        }
	        fr.close();
	        br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return readBag;
	}
	
	public static void writeToFile(String text, String fileName) {
		File outputFile = new File(fileName);
		try {
			FileWriter fw = new FileWriter(outputFile);
			fw.write(text);
			fw.close();
		}
		catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	public static void writeToFile(BagOfWords bagOfWords, String fileName) {
		File outputFile = new File(fileName);
		try {
			FileWriter fw = new FileWriter(outputFile);
			for(int i = 0; i < bagOfWords.size(); i++) { 
				fw.write(bagOfWords.get(i).word + "," + bagOfWords.get(i).count + "," + bagOfWords.get(i).percent);
				if(i < bagOfWords.size() - 1)
					fw.write("\n");
			}
			fw.close();
		}
		catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	/*
	public static void writeToFile(BagOfWords bagOfWords, String fileName, String option) {
		if(option.equalsIgnoreCase("wordList")) {
			File outputFile = new File(fileName);
			try {
				FileWriter fw = new FileWriter(outputFile);
				for(int i = 0; i < bagOfWords.size(); i++) { 
					fw.write(bagOfWords.get(i).word);
					if(i < bagOfWords.size() - 1)
						fw.write("\n");
				}
				fw.close();
			}
			catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
			}
		} else {
			System.out.println("Error: Invalid option for writeToFile");
		}
	}
	*/
	
	public static void writeToFile(ArrayList<String> strings, String fileName) {
		File outputFile = new File(fileName);
		try {
			FileWriter fw = new FileWriter(outputFile);
			for(int i = 0; i < strings.size(); i++) { 
				if(i == 0)
					fw.write(strings.get(i));
				else
					fw.write("\n" + strings.get(i));
				
			}
			fw.close();
		}
		catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	public static void writeDataFile(ArrayList<int[]> trainingData, String fileName) {
		File outputFile = new File(fileName);
		int colCount = trainingData.get(0).length;
		try {
			FileWriter fw = new FileWriter(outputFile);
			for(int i = 0; i < trainingData.size(); i++) { 
				String entry = "";
				int[] entryData = trainingData.get(i);
					for(int j = 0; j < colCount; j++) {
						entry += Integer.toString(entryData[j]);
						if(j < colCount - 1)
							entry += ",";
					}
				if(i == 0)
					fw.write(entry);
				else
					fw.write("\n" + entry);			
			}
			fw.close();
		}
		catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	public static void appendToFile(String text, String fileName) {
		File outputFile = new File(fileName);
		try {
			FileWriter fw = new FileWriter(outputFile, true);
				fw.write("\n" + text);
			fw.close();
		}
		catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	public static boolean isFileThere(String fileName) {
		return new File(fileName).isFile();
	}
	
	/*
	 * Special case. Read all files in a directory assuming they all hold bags stored as text
	 * Reads each bag, extracts the words (ignoring counts) and collects a unique list of all words found
	 */
	public static ArrayList<BagOfWords> readBagsInDirectory(String directoryName) {
		ArrayList<BagOfWords> allBags = new ArrayList<BagOfWords>();
		File directory = new File(directoryName);
		File[] fileList = directory.listFiles();
		for(File file : fileList) {
			//System.out.println(file);
			BagOfWords fileBag = readBagFile(file.toString());
			if(fileBag.size() > 0) {
				//System.out.println(file);
				allBags.add(fileBag);
			}
		}
		return allBags;
	}

	public static ArrayList<String> readFileNames(String directoryName) {
		ArrayList<String> fileNames = new ArrayList<String>();
		File directory = new File(directoryName);
		File[] fileList = directory.listFiles();
		for(File file : fileList) {
			//System.out.println(file); 
			String fileWord = file.getName().replace(".txt", "");
			fileNames.add(fileWord);
		}
		return fileNames;
	}
	
}
