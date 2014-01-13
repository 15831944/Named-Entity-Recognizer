import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class websiteExtractor {
	
	final static String LINK_HTML = "<a href=\"";
	static String classRootURL;

	/**
	 * @param args
	 */
	public static File extract(String assignedURL, String outputFileName) {
		classRootURL = assignedURL;
		ArrayList<URI> openLinks = new ArrayList<URI>();
		ArrayList<URI> closedLinks = new ArrayList<URI>();
		ArrayList<URI> ignoredLinks = new ArrayList<URI>();
		File  rawText = new File(outputFileName);
		try {		
			URI homeURI = new URI(classRootURL);
			FileWriter fw = new FileWriter(rawText);
			
			openLinks.add(homeURI);
			while(!openLinks.isEmpty()) {
				URI page = openLinks.get(0);
				openLinks.remove(0);
				if(!closedLinks.contains(page))
					closedLinks.add(page);
				
				//verify getting text/html
				String URIDataType = page.toURL().openConnection().getContentType();
				if(!URIDataType.contains("text/html")) {
					System.out.println("Unusuable data type: " + URIDataType + " at " + page +  " Skipping.");
					continue;
				}
				
		        BufferedReader in = new BufferedReader(
		        new InputStreamReader(page.toURL().openStream()));
				ArrayList<URI> newLinks = new ArrayList<URI>();
		        String inputLine;
		        while ((inputLine = in.readLine()) != null) {
		            //System.out.println(inputLine);
		            if(hasLink(inputLine)) {
		            	//System.out.println(inputLine + " " + parseLink(page, inputLine));
		            	newLinks.add(parseLink(page, inputLine));          	
		            }
		        }
		        in.close();
		        System.out.println("\nFinished reading: " + page.toString());
		        
		        updateLinkStatus(newLinks, openLinks, closedLinks, ignoredLinks, homeURI);
		        if(openLinks.size() == 0) 
		        	System.out.println("Completed scraping " + homeURI);
		        
		        //System.out.println("New-----------------------");
		        //displayLinks(newLinks);
		        //System.out.println("Open----------------------");
		        //displayLinks(openLinks);
		        //System.out.println("Closed---------------------");
		        //displayLinks(closedLinks);
		        //System.out.println("Ignored---------------------");
		        //displayLinks(ignoredLinks);
		        
		        //parse text from HTML to raw text
		        try { //separate try block so while loop still runs if we find a non-html file
	            	//String onlyText = Jsoup.parse(inputLine).text();
	            	//System.out.println(onlyText);
	            	Document doc = Jsoup.connect(page.toString()).get();
	            	//System.out.println(doc.text());
	            	fw.write(doc.text());
		        }
				catch (IOException e) {
					System.out.println("IOException: " + e.toString()); //this is OK
				}
			}//end of while
			fw.close();
		}
		catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
		return rawText;
		
		
	}
	
	public static void updateLinkStatus(ArrayList<URI> newLinks, ArrayList<URI> openLinks, ArrayList<URI> closedLinks, ArrayList<URI> ignoredLinks, URI homeURI) {
		String minimumDomain = getURIFileLocation(homeURI).toString();
		for(int i = 0; i < newLinks.size(); i++) {
			//check if already visited, or if goes above the URI height of where we started
			if(!newLinks.get(i).toString().contains(minimumDomain)) {
				if(!ignoredLinks.contains(newLinks.get(i)))
					ignoredLinks.add(newLinks.get(i));
			}
			else
				if(!closedLinks.contains(newLinks.get(i)) && !openLinks.contains(newLinks.get(i))) //add it if not on closed list and not already on open list
					openLinks.add(newLinks.get(i));
		}
		Collections.sort(openLinks, new URIComparator()); //maybe unnessesary
        Collections.sort(closedLinks, new URIComparator()); //maybe uneccesary
	}
	
	public static void displayLinks(ArrayList<URI> URIs) {
		for(int i = 0; i < URIs.size(); i++) {
			System.out.println(URIs.get(i));
		}
	}
	
	public static boolean hasLink(String line) {
		return (line.contains(LINK_HTML));
	}
	
	//enable multiple links per line?
	public static URI parseLink(URI page, String line) {
		int beginIndex = line.indexOf(LINK_HTML) + LINK_HTML.length();
		int endIndex = line.indexOf("\"", beginIndex);
		URI link = null;
		page = page.resolve(".");
		try {
			link = new URI(line.substring(beginIndex, endIndex));
			link = page.resolve(link);
		}
		catch (Exception e) {
			System.out.println("Error inn parseLink():");
		}
		
		/*
		if(!link.contains("//")) {
			link = getURIFileLocation(page) + link;
		}
		*/
		return link;
	}
	
	public static URI getURIFileLocation(URI link) {
		if(isURIFile(link)) {
			String text = link.toString();
			text = text.substring(0, text.lastIndexOf("/") + 1);
			try {
				link = new URI(text);
			}
			catch (Exception e) {
				System.out.println("getURIFileLocation error:");
			}
		}
		//System.out.println(link);
		return link;
	}
	
	public static boolean isURIFile(URI link) {
		int tailIndex = link.toString().lastIndexOf("//");
		return (link.toString().substring(tailIndex, link.toString().length()).contains("."));
	}

}

