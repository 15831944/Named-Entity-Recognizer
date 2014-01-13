import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class WebSearchUtils {
	/*
	 * Do we have any OS assumptions or detections?
	 * utorrent search is non-deterministic so far..
	 * 1. do iteration looking for binary
	 * 2. if none, open link with good sounding title or link
	 * 3. keep list of links and we can go back to the ones we didn't try
	 * 4.    clean up steps, skip bizzare urls etc.
	 * 5. eventually we should hit it
	 */
	public static void findInstaller(URL rootURL) {
		try {
			System.out.println("Finding download page from: " + rootURL);
			Document doc = Jsoup.connect(rootURL.toString()).get();
			Elements links = doc.select("a[href]");

			//Get Level 1 links
			ArrayList<URL> candidateDownloadPages = new ArrayList<URL>();
	        for (int i = 0; i < links.size(); i++) {
	        	Element link = links.get(i);
	        	String linkString = link.attr("abs:href");
	        	String linkName = link.text();
	        	
	        	if(linkString.toLowerCase().contains("download") || linkName.toLowerCase().contains("download")) {
	        		//System.out.println(linkString + " " + linkName);
	        		candidateDownloadPages.add(new URL(linkString));
	        	}
	        }//end of for
	        
	        //Loop dig until reach the page with the binary since sometimes theres another 'which os' etc kind of page
	        //always assuming the first good match is the right one, not iterating through them all or anything.
	        //too much unique html, try add iteration until we find non-html link
	        if(candidateDownloadPages.size() > 0) {
		        URL currentLevelURL = candidateDownloadPages.get(0); 
		        System.out.println("Investigating next level page: " + currentLevelURL + " " + currentLevelURL.openConnection().getContentType());
		        for(int levelDepth = 2; currentLevelURL != null && currentLevelURL.openConnection().getContentType().contains("html") && levelDepth < 5; levelDepth++) {
					doc = Jsoup.connect(currentLevelURL.toString()).get();
					//System.out.println(doc);
					links = doc.select("a[href]");
					URL lastURL = currentLevelURL;
					currentLevelURL = null;
					for (int i = 0; i < links.size() && currentLevelURL == null; i++) {
						Element link = links.get(i);
			        	String linkString = link.attr("abs:href");
			        	String linkName = link.text();
			        	//TODO tweak this to get the rough download link. Move this to the file downloader?
			        	//lazy hack to get utorrent to download. Need better idea. Will I need NLP to do this? Maybe just iterate until we find the binary
			        	if((linkName.toLowerCase().contains("download") || linkName.toLowerCase().contains("click here")) && !linkString.contentEquals(lastURL.toString())) {
			        		System.out.println("Searching level " + levelDepth + " new link: " + linkString + "     because of name     " + linkName);
			        		currentLevelURL = new URL(linkString);
			        	}
					}
					/*
					//display all possible links from this level
					for (int i = 0; i < links.size(); i++) {
						Element link = links.get(i);
			        	String linkString = link.attr("abs:href");
			        	String linkName = link.text();
			        	System.out.println("Searching level " + levelDepth + " found link: " + linkString + "           " + linkName);
	
					}
					*/
		        
		        }//end of level digging loop
		        System.out.println("Assuming download link is: " + currentLevelURL);
		        if(currentLevelURL != null)
		        	robustFileDownloader(currentLevelURL, doc.title());
	        }
	        else {
	        	System.out.println("Error. No candidate pages.");
	        }
	        
	        
	        /*
        	if(linkName.toLowerCase().contains("download")) {
        		System.out.println("Assuming link is: " + linkString + "     because of name     " + linkName);
        		downloadURL = new URL(linkString);
        		robustFileDownloader(downloadURL, doc.title());
        	}
        	*/

		}  
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/*
	 * File downloading modified from http://buddhimawijeweera.wordpress.com/2013/05/18/parsing-html-pages-with-jsoup/
	 * what about .msi files?
	 */
	public static void robustFileDownloader(URL dirtyURL, String saveName) {
		try {
			//get file length
			URLConnection connection = dirtyURL.openConnection();
			connection.connect();
			float bytesPerMB = 1048576;
			int typicalMbs = 5;
			float fileLength = connection.getContentLength()/bytesPerMB;
			String name = connection.getHeaderField("Content-Disposition").substring(connection.getHeaderField("Content-Disposition").indexOf("\"") + 1);
			name = name.replaceAll("\"", "");
			saveName = name;
			/*
			if(connection.getContentType().contains("application")) //"application/x-msdos-program"
				saveName += ".exe";
			else
				saveName += ".xxx";
			*/
			System.out.println("Downloading " + saveName + " from " + dirtyURL); //add file size and timer
			System.out.println("File type: " + connection.getContentType() + " Name: " + saveName);
			System.out.println("Size: " + fileLength + " MB, ETA " + fileLength/typicalMbs*8 + " seconds at " + typicalMbs + " Mb/s.");
			
			//timer(); //TODO add timer/downloaded size count while waiting
			
            // Read file
            InputStream in = new BufferedInputStream(dirtyURL.openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;
            while (-1 != (n = in.read(buf))) {
                out.write(buf, 0, n);
            }
            out.close();
            in.close();
            byte[] response = out.toByteArray();

            // Save images
            FileOutputStream fos = new FileOutputStream(saveName);
            fos.write(response);
            fos.close();
            System.out.println("Download complete.\n");
        } catch (Exception e) {
            System.out.println("Error in reading & storing: "+e.getMessage());
        }
	}
	
	/*
	 * Just for testing code. Can be deleted without changing functionality
	 */
	public static void downloadWebpage(String urlString) {
	    InputStream is = null;
	    BufferedReader br;
	    String line;
	
	    try {
	        URL url = new URL(urlString);
	        is = url.openStream();  // throws an IOException
	        br = new BufferedReader(new InputStreamReader(is));
	
	        while ((line = br.readLine()) != null) {
	            System.out.println(line);
	        }
	    } catch (MalformedURLException mue) {
	         mue.printStackTrace();
	    } catch (IOException ioe) {
	         ioe.printStackTrace();
	    } finally {
	        try {
	            if (is != null) is.close();
	        } catch (IOException ioe) {
	            // nothing to see here
	        }
	    }
	}
	
	/*
	 * Code modified from http://jsoup.org/cookbook/extracting-data/example-list-links
	 */
	public static ArrayList<URL> searchBing(String searchTerm) {
		System.out.println("Searching the web for " + searchTerm);
		ArrayList<URL> searchResults = new ArrayList<URL>();
		final String BING_ROOT = "http://www.bing.com/search?q=";
		try {
			URI page = new URI(BING_ROOT + searchTerm);

			//Trim all the junk links before the actual results
			int resultsStartIndex = Jsoup.connect(page.toString()).get().toString().indexOf("<div id=\"results\">");
			String resultsHTML = Jsoup.connect(page.toString()).get().toString().substring(resultsStartIndex);
			Document doc = Jsoup.parse(resultsHTML);
			//System.out.println(doc.text());

			Elements links = doc.select("a[href]");
			//System.out.println(links.size());
	        for (int i = 0; i < links.size(); i++) {
	        	Element link = links.get(i);
	        	String linkString = link.attr("abs:href");
	        	
	        	if(!linkString.contains("bing") && linkString.length() > 0) {
	        		searchResults.add(new URL(linkString));
	        		//System.out.println();   
		            //System.out.println(i + " " + linkString);
		            //System.out.println(link.text());
	        	}
	        }
			
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		if(searchResults.size() > 0)
			System.out.println("Top search result was: " + searchResults.get(0));
		else
			System.out.println("No results found.");
		return searchResults;
	}

	
}
