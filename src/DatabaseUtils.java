import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseUtils {
	
	/*
	 * Searches my offline wiki database for an article and returns the rawtext of it's article contents
	 * Considers getting multiple articles with the same name or no articles a failure and returns null in that case
	 */
	public static String searchWikiDB(String searchTerm) throws SQLException {
		//searchTerm = searchTerm.replace(' ', '_');
		System.out.println("Search database for: " + searchTerm);
		final Connection CONN;
		ResultSet results;
		int port = 3306; 
		String username = "root";
		String password = "";
		String sqlQuery = 
				"SELECT page_title, page_len, si_text" +
				" FROM page, searchindex" + 
				" WHERE page_id=si_page" +
				" AND MATCH(si_title) AGAINST('" + searchTerm + "' IN boolean MODE)" +
				" AND page_is_redirect=0 AND page_namespace IN (0)" +
				" AND si_title = '" + searchTerm + "'" +
				" limit 5; ";
		int numCol;
		int numRow;
		
		System.out.println(sqlQuery);
		
		String connectionURL = String.format("jdbc:mysql://localhost:%d/my_wiki", port);
		CONN = DriverManager.getConnection(connectionURL, username, password);
		
		Statement sqlStmt = CONN.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		results = sqlStmt.executeQuery(sqlQuery);
		
		numCol = results.getMetaData().getColumnCount();
		results.last();
		numRow = results.getRow();
		results.beforeFirst();

		
		System.out.println("Search complete. Found " + numRow);
		//display all results
		while(results.next()) {
			for(int i = 1; i < numCol; i++) {
				System.out.print(results.getString(i) + " ");
			}
			System.out.println(results.getString(numCol).length());
			//System.out.println();
		}
		
		//return null as an error if results are invalid
		//results are invalid if there is anything but exactly 1 result (1 article hit)
		if(numRow == 0)
			return null;
		else if(numRow > 1)
			return null;
		else  {
			results.first();
			return results.getString(numCol);
		}
	}
	
	public static String[] parseWikiArticle(String rawtext) {
		//System.out.println("\nOldText:\n" + rawtext);
		
		rawtext = rawtext.replace("u800", "");
		
		//System.out.println("\nNewText:\n" + rawtext);
		
		return rawtext.split("\\s+");
	}

}//end of class
