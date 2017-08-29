package wordnet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;

public class TestJAWS{
	
	public static String getWordSynonyms( String word ){
	   	 File f=new File("C:\\Users\\Hospice\\WordNet\\3.0\\dict");
	        System.setProperty("wordnet.database.dir", f.toString());
	        //setting path for the WordNet Directory

	        WordNetDatabase database = WordNetDatabase.getFileInstance();
	        Synset[] synsets = database.getSynsets(word);
	        //  Display the word forms and definitions for synsets retrieved
	        ArrayList<String> al = new ArrayList<String>();

	        if (synsets.length > 0){
	           // add elements to al, including duplicates
	           HashSet hs = new HashSet();
	           for (int i = 0; i < synsets.length; i++){
	              String[] wordForms = synsets[i].getWordForms();
	                for (int j = 0; j < wordForms.length; j++)
	                {
	                  al.add(wordForms[j]);
	                }


	           //removing duplicates
	            hs.addAll(al);
	            al.clear();
	            al.addAll(hs);
	           }
	           
	           }
	        String wordexp = "";
	        for (String s:al){
	        	wordexp+= s+ " ";
	        	//System.out.println(s);
	        }
	      //else
	        //{
	       //  System.err.println("No synsets exist that contain the word form '" + wordForm + "'");
	      //  }
	        //System.out.println(wordexp);
			return wordexp; 	
		}
	
	public static ArrayList<String> getSynonyms( String word ){
   	 File f=new File("C:\\Users\\Hospice\\WordNet\\3.0\\dict");
        System.setProperty("wordnet.database.dir", f.toString());
        //setting path for the WordNet Directory

        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets = database.getSynsets(word);
        //  Display the word forms and definitions for synsets retrieved
        ArrayList<String> al = new ArrayList<String>();

        if (synsets.length > 0){
           // add elements to al, including duplicates
           HashSet hs = new HashSet();
           for (int i = 0; i < synsets.length; i++){
              String[] wordForms = synsets[i].getWordForms();
                for (int j = 0; j < wordForms.length; j++)
                {
                  al.add(wordForms[j]);
                }


           //removing duplicates
            hs.addAll(al);
            al.clear();
            al.addAll(hs);
           }
           
           }
        String wordexp = "";
        for (String s:al){
        	wordexp+= s+ " ";
        	System.out.println(s);
        }
      //else
        //{
       //  System.err.println("No synsets exist that contain the word form '" + wordForm + "'");
      //  }
		return al; 	
	}
	
    public static void main(String[] args){
    	getWordSynonyms("capacity");
          String wordForm = "Michaelis";
          //  Get the synsets containing the word form=capicity

         File f=new File("C:\\Users\\Hospice\\WordNet\\3.0\\dict");
         System.setProperty("wordnet.database.dir", f.toString());
         //setting path for the WordNet Directory

         WordNetDatabase database = WordNetDatabase.getFileInstance();
         Synset[] synsets = database.getSynsets(wordForm);
         //  Display the word forms and definitions for synsets retrieved

         if (synsets.length > 0){
            ArrayList<String> al = new ArrayList<String>();
            // add elements to al, including duplicates
            HashSet hs = new HashSet();
            for (int i = 0; i < synsets.length; i++){
               String[] wordForms = synsets[i].getWordForms();
                 for (int j = 0; j < wordForms.length; j++)
                 {
                   al.add(wordForms[j]);
                 }


            //removing duplicates
             hs.addAll(al);
             al.clear();
             al.addAll(hs);

            //showing all synsets
            for (int i1 = 0; i1 < al.size(); i1++) {
                  System.out.println(al.get(i1));
            }
         }
    }
    }

    //else
    //{
   //  System.err.println("No synsets exist that contain the word form '" + wordForm + "'");
  //  }
    
    
    
} 