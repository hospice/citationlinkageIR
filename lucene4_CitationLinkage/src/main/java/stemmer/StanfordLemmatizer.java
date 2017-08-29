package stemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.WordnetSynonymParser;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import wordnet.TestJAWS;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class StanfordLemmatizer {

    protected StanfordCoreNLP pipeline;
	public static Set<String> engStopwords = new HashSet<String>();
    TestJAWS testJAWS = new TestJAWS();

    public StanfordLemmatizer() {
        // Create StanfordCoreNLP object properties, with POS tagging
        // (required for lemmatization), and lemmatization
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        props.setProperty("customAnnotatorClass.stopword", "intoxicant.analytics.coreNlp.StopwordAnnotator");

        // StanfordCoreNLP loads a lot of models, so you probably
        // only want to do this once per execution
      //  WordnetSynonymParser wnsp = new WordnetSynonymParser(true, true, new StandardAnalyzer());
        this.pipeline = new StanfordCoreNLP(props);
       // loadStopwords();
        //CharArraySet stopSet = CharArraySet.copy(StandardAnalyzer.STOP_WORDS_SET);
      //  stopSet.addAll(engStopwords);
    }

    public List<String> lemmatize(String documentText)
    {
        List<String> lemmas = new LinkedList<String>();

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(documentText);

        // run all Annotators on this text
        this.pipeline.annotate(document);

        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the list of lemmas
            	String word = token.get(LemmaAnnotation.class);
                lemmas.add((word));
                System.out.println(word+": "+this.testJAWS.getWordSynonyms(word));
                //System.out.println(token.get(LemmaAnnotation.class));
            }
        }

        return lemmas;
    }
    public String lemmatizeText(String documentText)
    {
    	
    	//PorterStemmer s = new PorterStemmer();
       // List<String> lemmas = new LinkedList<String>();
    	String lemmaText = "";
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(documentText.replaceAll("[^a-zA-Z0-9]", " "));
        Set<?> stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
        loadStopwords();
       // CharArraySet stopWords = CharArraySet.copy(StandardAnalyzer.STOP_WORDS_SET);
        //stopWords.addAll(engStopwords);

        // run all Annotators on this text
        this.pipeline.annotate(document);

        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the list of lemmas
                //lemmas.add(token.get(LemmaAnnotation.class));
               // System.out.println(token.get(LemmaAnnotation.class));
               String word = token.get(LemmaAnnotation.class);
               if (!stopWords.contains(word)) {
            	//lemmaText += s.stripAffixes(word)+" ";
            	  // word = this.testJAWS.getWordSynonyms(word);
            	   lemmaText += word+" ";
               }
            }
        }

        return lemmaText;
    }
    public String lemmatizeStemText(String documentText)
    {
    	PorterStemmer s = new PorterStemmer();
    	//PaiceStemmer pStemmer = new PaiceStemmer();
       // List<String> lemmas = new LinkedList<String>();
    	String lemmaStemText = "";
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(documentText.replaceAll("[^a-zA-Z0-9]", " "));
        Set<?> stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
        loadStopwords();
       // CharArraySet stopWords = CharArraySet.copy(StandardAnalyzer.STOP_WORDS_SET);
       // stopWords.addAll(engStopwords);
        // run all Annotators on this text
        this.pipeline.annotate(document);

        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the list of lemmas
                //lemmas.add(token.get(LemmaAnnotation.class));
               // System.out.println(token.get(LemmaAnnotation.class));
               String word = token.get(LemmaAnnotation.class);
               if (!stopWords.contains(word)) {
            	   //word = this.testJAWS.getWordSynonyms(word);
            	  // for(String w: word.split(" ")){
            		//   word = w;
            	   lemmaStemText += s.stripAffixes(word)+" ";
            	  // lemmaStemText += word+" ";
            	  // }
               }
            }
        }

        return lemmaStemText;
    }
    public static void loadStopwords()
	{
		File file = null;
		BufferedReader inputStream = null;
		try {
			file = new File("C:/Users/Hospice/backup/manyaspects_alphawks/manyaspects_alphawks/config/stopword.list");
			inputStream = new BufferedReader(new FileReader(file));
			String line;
			while ((line = inputStream.readLine()) != null) {
				//System.out.println(line);
				if ((line.length() == 0) || (line.trim().length() == 0))
					continue;
				engStopwords.add(line.trim());

			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (inputStream != null)
				try {
					inputStream.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
		}
	}
    
    public static void main(String args[]){
    	
    	StanfordLemmatizer lemmatizer = new StanfordLemmatizer();
    	String documentText = "Hybrid laccases combining Ascomycotina sequences and positively-selected sites identified in Basidiomycotina could prove useful for testing new physico-chemical properties for biotechnology applications.";
    	System.out.println(lemmatizer.lemmatize(documentText));
    }
}