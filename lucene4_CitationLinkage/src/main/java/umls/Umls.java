package umls;

import java.sql.DriverManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//import org.junit.Test;


public class Umls {
	public Umls(){
		
	}
	
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	private String databaseconnect = "jdbc:mysql://localhost/umls2010?"+"user=root&password=";
	public String getCUI(String word) throws ClassNotFoundException, SQLException{
		String cui ="";
		String command = "select distinct CUI from mrconso where STR =" + "'"+word+"'" ;
    	Class.forName("com.mysql.jdbc.Driver");
		// Setup the connection with the DB
		connect = DriverManager.getConnection(databaseconnect);

		// Statemnet allow to issue SQL queries to the database
		statement = connect.createStatement();
		resultSet = statement .executeQuery(command);
		while(resultSet.next()){
			cui += resultSet.getString("CUI") + " ";
		}
		close();
		//System.out.println(cui);
		return cui;
		
	}
	public String getDef(String word) throws ClassNotFoundException, SQLException{
		String[] cuis = getCUI(word).split(" ");
		for (String cui:cuis){
			String command = "select  DEF from mrdef where CUI =" + "'"+cui+"'" ;
	    	Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			//connect = DriverManager.getConnection("jdbc:mysql://localhost/tm?"+"user=root&password=");
			connect = DriverManager.getConnection(databaseconnect);

			// Statemnet allow to issue SQL queries to the database
			statement = connect.createStatement();
			resultSet = statement.executeQuery(command);
			while(resultSet.next()){
				System.out.println(resultSet.getString("DEF") );
			}
		}
		return word;
		
		
	}
	public String getWordSet(String word) throws ClassNotFoundException, SQLException{
		String[] cuis = getCUI(word).split(" ");
		for (String cui:cuis){
			String command = "select distinct STR from mrconso where CUI =" + "'"+cui+"'" ;
	    	Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			//connect = DriverManager.getConnection("jdbc:mysql://localhost/tm?"+"user=root&password=");
			connect = DriverManager.getConnection(databaseconnect);

			// Statemnet allow to issue SQL queries to the database
			statement = connect.createStatement();
			resultSet = statement .executeQuery(command);
			while(resultSet.next()){
				System.out.println(resultSet.getString("STR") );
			}
		}
		return word;
		
		
	}
	
	public String getWordWithCui(String cui) throws ClassNotFoundException, SQLException{
		String command = "select  STR from mrconso where CUI =" + "'"+cui+"'" ;
    	Class.forName("com.mysql.jdbc.Driver");
		// Setup the connection with the DB
		//connect = DriverManager.getConnection("jdbc:mysql://localhost/tm?"+"user=root&password=");
		connect = DriverManager.getConnection(databaseconnect);

		// Statemnet allow to issue SQL queries to the database
		statement = connect.createStatement();
		resultSet = statement .executeQuery(command);
		while(resultSet.next()){
			System.out.println(resultSet.getString("STR") );
		}
		return cui;
		
		
	}
	public void getConcept(String word) throws ClassNotFoundException, SQLException{
		
		String[] cuis = getCUI(word).split(" ");
		for (String cui:cuis){
			String command = "select  STY from mrsty where CUI =" + "'"+cui+"'" ;
	    	Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			//connect = DriverManager.getConnection("jdbc:mysql://localhost/tm?"+"user=root&password=");
			connect = DriverManager.getConnection(databaseconnect);

			// Statemnet allow to issue SQL queries to the database
			statement = connect.createStatement();
			resultSet = statement .executeQuery(command);
			while(resultSet.next()){
				System.out.println(resultSet.getString("STY") );
			}
			close();
		}
		
	}
	
public String getCategory(String word) throws ClassNotFoundException, SQLException{
	String category = "";
		String[] cuis = getCUI(word).split(" ");
		for (String cui:cuis){
			String command = "select  STY from mrsty where CUI =" + "'"+cui+"'" ;
	    	Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			//connect = DriverManager.getConnection("jdbc:mysql://localhost/tm?"+"user=root&password=");
			connect = DriverManager.getConnection(databaseconnect);

			// Statemnet allow to issue SQL queries to the database
			statement = connect.createStatement();
			resultSet = statement .executeQuery(command);
			
			while(resultSet.next()){
				//System.out.println(resultSet.getString("STY") );
				category += resultSet.getString("STY") +"/n";
			}
			close();
		}
		return category;
	}
	
	
	private void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (statement != null) {
				statement.close();
			}

			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {

		}
	}
	
	
	//@Test
	public void testCui() throws ClassNotFoundException, SQLException{
		getCUI("pH");
	}
	
	//@Test
	public void testDef() throws ClassNotFoundException, SQLException{
		getDef("protocol");
	}
	//@Test
	public void testgetWordSet() throws ClassNotFoundException, SQLException{
		getWordSet("Lung Cancer");
	}
	
	//@Test
	public void getWordWithCui() throws ClassNotFoundException, SQLException{
		getWordWithCui("C0202304");
	}
	//@Test
    public void getConcept() throws ClassNotFoundException, SQLException{
    	
    	getConcept("Fixation");
    }
    public static void main(String[] args)throws ClassNotFoundException, SQLException{
		Umls umls = new Umls();
		//umls.getConcept();
		
		umls.getConcept("laccase");
	}
}
