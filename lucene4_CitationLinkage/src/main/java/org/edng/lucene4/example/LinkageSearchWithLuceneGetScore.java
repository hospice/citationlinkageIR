package org.edng.lucene4.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

//import linkagecandidate.LinkageFiles;



import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.AfterEffectL;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BasicModelIF;
import org.apache.lucene.search.similarities.DFRSimilarity;
import org.apache.lucene.search.similarities.DistributionLL;
import org.apache.lucene.search.similarities.IBSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.LambdaDF;
import org.apache.lucene.search.similarities.LambdaTTF;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.NormalizationH1;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import chunk.PhraseChunker;

import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Files;

import eval.WeightedNormalizedDiscountedCumulativeGain;



//import com.aliasi.hmm.HmmDecoder;
//import com.aliasi.tokenizer.TokenizerFactory;
//import com.aliasi.util.Files;
import org.junit.Test;

import sentences.SentenceBoundary;

public class LinkageSearchWithLuceneGetScore {

	private int numSentences;
	private static double score = 0;
	private static int number = 0;

	public static Set<String> stopwords = new HashSet<String>();
	
	public  ArrayList precision = new ArrayList();
	public  ArrayList WNCG = new ArrayList();
	/*private static BM25Similarity similarity = null;
	public LinkageSearchBM25(Directory index) {

		//super(index);
		//analyzer = new StopAnalyzer(Version.LUCENE_32);
		//analyzer = new SimpleAnalyzer(Version.LUCENE_32);
         //analyzer = new StandardAnalyzer();
		// analyzer = new StandardAnalyzer(Version.);
		//BM25Similarity similarity = new BM25Similarity();		
		//BasicSimilarityProvider similarityProvider = new BasicSimilarityProvider(similarity);
		//this.setSimilarityProvider(similarityProvider);
          //similarity = new BM25Similarity(1.2f, 100f);
		// TODO Auto-generated constructor stub
		this.analyzer = new StandardAnalyzer();
	}*/
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
				stopwords.add(line.trim());

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

	@SuppressWarnings("deprecation")
	@org.junit.Test
	public void runTest() throws Exception {
		loadStopwords();

		@SuppressWarnings("deprecation")
		CharArraySet stopSet = CharArraySet.copy(StandardAnalyzer.STOP_WORDS_SET);

		stopSet.addAll(stopwords);


		List<String> sentences = new ArrayList<String>();
		@SuppressWarnings("deprecation")
		StandardAnalyzer analyzer = new StandardAnalyzer(stopSet);
		//SnowballAnalyzer  sba = new SnowballAnalyzer(Version.LATEST, "English");

		//PayloadAnalyzer analyzer = new PayloadAnalyzer();

		//  TextField textField = new TextField("content", "", Field.Store.YES);

		String fileName = "C:/Users/Hospice/Downloads/Thesis/annotateSarah3/sarahfiles/annotationdata1.txt";
		String textFromFile = Files.readFromFile(new File(fileName),"ISO-8859-1");

		double wNDCGValAg = 0;
		String[] listFromFile = textFromFile.split("\n");
		for (String line:listFromFile){
			
			IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);

			//BM25Similarity similarity = new BM25Similarity(1.2f, 100f);

			IBSimilarity similarity = new IBSimilarity(new DistributionLL(), new LambdaDF(), new NormalizationH1());  //Best?

			//DFRSimilarity similarity = new DFRSimilarity(new BasicModelIF(), new AfterEffectL(), new NormalizationH1());

			//LMJelinekMercerSimilarity similarity = new LMJelinekMercerSimilarity(0.01f);

			//LMDirichletSimilarity similarity = new LMDirichletSimilarity();

			//DefaultSimilarity similarity = new DefaultSimilarity(); //Best or second?


			config.setSimilarity(similarity);

			Document doc = new Document();
			
		
			RAMDirectory ramdir = new RAMDirectory();
			IndexWriter indexWriter = new IndexWriter(ramdir, config);

			//System.out.println(line);
			String lineValues[] = line.split("\t");
			Map<String, Double> getTextFrequencyMap = new HashMap<String, Double>();
			List<String> wordsList = new ArrayList<String>();
			//String textFile = "C:/Users/Hospice/articlescitedfilesbiochem/4_1471-2199-9-9.txt";
			String textFile = lineValues[0];
			String citationText = lineValues[1];
			String textString = FileUtils.readFileToString(new File(textFile), "UTF-8");
			SentenceBoundary sentenceBoundary = new SentenceBoundary();
			sentences = sentenceBoundary.sentence(textString.replaceAll("(^.*[^\\.?!])\r?\n", " ").replaceAll("  +", " "));

			for (int i = 0; i< sentences.size(); i++){
				Document doc2 = new Document();
				String [] textSplit = sentences.get(i).toLowerCase().split("\n");
				int tLength = textSplit.length;
				doc2.add(new Field("content",textSplit[tLength-1], Store.YES, Index.ANALYZED));
				//System.out.println(sentences.get(i));

				indexWriter.addDocument(doc2);
			}
			  	  
			
			indexWriter.commit();
			int count = 0;
			HashMap<String, Double> arrayLMap = new HashMap<String, Double>();
			linkage.LinkageFiles lf = new linkage.LinkageFiles();

			arrayLMap = lf.loadLinkageFileMap(lineValues[2].trim());

			//linkageSearchBM25.setNumSentences(arrayLMap.size());

			int precisionNumber = arrayLMap.size();

			SortedMap<Float,String> sentenceMap = 
					new TreeMap<Float,String>();
			IndexReader indexReader = DirectoryReader.open(ramdir);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);		
		
			 
			  System.out.println(indexReader.numDocs());
			indexSearcher.setSimilarity(similarity);
			BooleanQuery booleanQuery = new BooleanQuery();
			
			BooleanQuery bQuery = new BooleanQuery();


			String [] wordInListSplit = citationText.toLowerCase().split(" ");
			
			System.out.println(citationText);
			HmmDecoder mPosTagger = null;
			TokenizerFactory mTokenizerFactory = null;
			
			PhraseChunker pChunker = new PhraseChunker(mPosTagger,mTokenizerFactory);
			ArrayList<String> pchuncker = new ArrayList<String>();
			
			pchuncker = pChunker.getCHunkNounPhrases(citationText);

			bQuery = (BooleanQuery) computeBooleanQueryNP(citationText.replace("[~%()]", " "));
			
			
			QueryParser queryParser = new QueryParser("content", analyzer);
	        Query query = queryParser.parse(citationText.replace("[~%()]", " "));

	        //Query query = queryParser.parse(pChunckerList);

	        BooleanQuery booleanQueryParse = new BooleanQuery();
	        
	        booleanQueryParse.add(query, Occur.SHOULD);
			for (String word: wordInListSplit){

				booleanQuery.add(new TermQuery(new Term("content", word)), Occur.SHOULD );
			}
	        
	        
			Term termInstance = new Term("content", wordInListSplit[0]);
			
			//long docCount = indexReader.docFreq(termInstance);
			
			
			//System.out.println("docCount " + wordInListSplit[0] +" "+ docCount);

		
			TopDocs topDocs = indexSearcher.search(booleanQuery, precisionNumber);
		
			for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
				//System.out.println( query.toString("content"));
				// System.out.println(scoreDoc.score);
				  System.out.println(scoreDoc.score);
				int docId = scoreDoc.doc;
				
				//indexSearcher.explain(query, docId);
				
				//System.out.println(indexSearcher.explain(query, docId));
				
				//System.out.println(indexSearcher.collectionStatistics("content"));
				doc = indexSearcher.doc(docId);
				 System.out.println(sentences.get(docId));
				System.out.println("Content :"+ doc.get("content"));

				//System.out.println(scoreDoc.score);

				// if(scoreDoc.score>=0.1F){

				sentenceMap.put((float) scoreDoc.doc, ">> "+StringUtils.chomp(doc.get("content")));
				//sentenceMap.put(1/scoreDoc.score,sentences.get(docId));
				// System.out.println(scoreDoc);
				// System.out.println(StringUtils.chomp(doc.get("text")));
				// }
				
				//System.out.println("==============================================================");
			}
			/*
        int count = 0;
		HashMap<String, Double> arrayLMap = new HashMap<String, Double>();
		linkage.LinkageFiles lf = new linkage.LinkageFiles();

		arrayLMap = lf.loadLinkageFileMap(lineValues[2].trim());

		//linkageSearchBM25.setNumSentences(arrayLMap.size());

		int precisionNumber = arrayLMap.size();*/

			String[] sentencesT =  sentenceMap.values().toArray(new String[0]);

			List<String> sentListTm = new LinkedList<String>(); // list of ranked sentences 
			for (String s: sentencesT){
				sentListTm.add(s.replaceAll(">> ","").trim().replaceAll("\\s+", " ").toLowerCase().trim().replaceAll("-"," ").toLowerCase().replaceAll("[- +]"," ")); //
				//System.out.println(s.replaceAll(">> ","").trim().replaceAll("\\s+", " ").toLowerCase().trim().replaceAll("-"," ").toLowerCase().replaceAll("[- +]"," "));
			}
			List<String> listCandidate = new ArrayList<String>(arrayLMap.keySet());  // list of candidate sentences

			HashMap<String, Double> hashMapFromRanking = new HashMap<String, Double>();


			for (int k = 0; k < precisionNumber; k++){
				//if (list.contains(sentences[k].trim().split("=")[0].toLowerCase().replaceAll("[- +]"," "))){
				if (sentListTm.contains(listCandidate.get(k).trim())){
				
				//if (listCandidate.contains(sentListTm.get(k).trim())){
					//if (sentListTm.indexOf(listCandidate.get(k)) <= listCandidate.size()){
						//System.out.println("Position >> "+k);
						//System.out.println("Position >> "+(k) + " " +sentListTm.indexOf(sentListTm.get(k))+"\t"+arrayLMap.get(sentListTm.get(k).trim().split("=")[0].toLowerCase().replaceAll("[- +]"," ")));
						//System.out.println(sentListTm.get(k) +" " +arrayLMap.get(listCandidate.get(k)));
						hashMapFromRanking.put(listCandidate.get(k).trim(), arrayLMap.get(listCandidate.get(k)));
						count++;
					//}
						System.out.print("Position: " +sentListTm.indexOf(listCandidate.get(k)));

						System.out.println("\t"+"Matched !!!" + arrayLMap.get(listCandidate.get(k).trim())+"\t"+ listCandidate.get(k).trim());

				}
				else {
					
					System.out.print("Position: " +sentListTm.indexOf(listCandidate.get(k)));
					System.out.println("\t"+arrayLMap.get(listCandidate.get(k).trim())+"\t"+ listCandidate.get(k).trim());
				}
			}
			WeightedNormalizedDiscountedCumulativeGain  wNDCG = new WeightedNormalizedDiscountedCumulativeGain();


			double wNDCGValue = 
					wNDCG.evaluateInstance(arrayLMap, hashMapFromRanking, 0);
			wNDCGValAg+=wNDCGValue;
			//System.out.println( StringUtils.join(sentences, "\n"));
			//output.write("Precision sentences # :"+ count+"\n");
			if (wNDCGValue == 0){
				System.out.println(Arrays.asList(listFromFile).indexOf(line) +" "+ line);
			}
			System.out.println("Precision sentences # :"+ count);
			//output.write("Precision at " +listCandidate.size() +" :"  +(double)count/precisionNumber+"\n"+"\n");
			System.out.println("Precision at " +listCandidate.size() +" :"  +(double)count/precisionNumber);
			System.out.println("WNCG: " +wNDCGValue);
			precision.add((double)count/precisionNumber);
			WNCG.add(wNDCGValue);
			System.out.println();
			//String str = Double.toString(score);
			double linkageScore = (double)count/precisionNumber;
			//score += (double)count/arrayL.size();

			// if (linkageScore >0){
			//System.out.println(lineNumber+"\t"+line.trim());
			//output+=line.trim()+"\n";
			score += linkageScore;
			number++;
			//System.out.println("Overrall Precision Score  " + score/number);

			// }
			if (linkageScore == 0){

			}
			//else{}



		}
		//output.write("Number of valid linkages  " + number+"\n");
		System.out.println("Number of valid linkages  " + number);
		//output.write("Overrall Precision Score  " + score/number);

		System.out.println("Overrall Precision Score  " + score/number);

		System.out.println("Overrall wNDCG Score  " +wNDCGValAg/number);

		Collections.sort(precision);
		Collections.sort(WNCG);
		
		System.out.println("precisions  " + precision);

		System.out.println("WNCGs  " +WNCG);

		/*

		System.out.println("Number Linkage Sentences  "+sentences.length);

		for (int k = 0; k< precisionNumber; k++){
			//if (arrayL.contains(sentences[k].replaceAll(">> ","").trim())){
			if (listCandidate.contains(list.get(k).trim().toLowerCase().replaceAll("[- +]"," "))){
				int t = k+1;
				System.out.println("Position >> "+t);
				//System.out.println("Position >> "+arrayLMap.entrySet().indexOf(sentences[k].replaceAll(">> ","").trim()));

				//System.out.println(sentences[k]+"\n"+(String)hmLink.get(sentences[k].replaceAll(">> ","").trim()));
				count++;

				//System.out.println("count >> " + count);

			}
		}

		System.out.println("count >> " + count);
		System.out.println(" ======================================================== ");
		 */

	}

	public void setNumSentences(int numSentences) {
		this.numSentences = numSentences;
	}

	public int getNumSentences(int numSentences) {

		return this.numSentences = numSentences;
	}
	

	public void buildIndex(Directory ramdir, String text) throws Exception {
		List<String> sentences = new ArrayList<String>();
		StandardAnalyzer analyzer = new StandardAnalyzer();

		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		BM25Similarity similarity = new BM25Similarity(1.2f, 100f);
		config.setSimilarity(similarity);
		IndexWriter indexWriter = new IndexWriter(ramdir, config);

		Document doc = new Document();
		TextField textField = new TextField("content", "", Field.Store.YES);


		SentenceBoundary sentenceBoundary = new SentenceBoundary();
		sentences = sentenceBoundary.sentence(text.replaceAll("(^.*[^\\.?!])\r?\n", " ").replaceAll("  +", " "));

		for (String content : sentences) {
			textField.setStringValue(content);
			doc.removeField("content");
			doc.add(textField);
			indexWriter.addDocument(doc);
		}
		indexWriter.commit();
	}
	public String[] searchIndex(Directory ramdir, Query query) 
			throws Exception {

		//  Text1 = Files.readFromFile(file,"ISO-8859-1");
		//List<String> sentences = sentenceBoundary.sentence(Text1);
		SortedMap<Float,String> sentenceMap = 
				new TreeMap<Float,String>();
		IndexReader indexReader = DirectoryReader.open(ramdir);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		BM25Similarity similarity = new BM25Similarity(1.2f, 100f);

		indexSearcher.setSimilarity(similarity);
		TopDocs topDocs = indexSearcher.search(query, this.numSentences);

		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {

			// System.out.println(scoreDoc.score);



			int docId = scoreDoc.doc;
			//System.out.println(searcher.explain(query, docId));
			Document doc = indexSearcher.doc(docId);
			// System.out.println(sentences.get(docId));
			//System.out.println(doc.get("text"));

			// System.out.println(doc.get("text"));

			// if(scoreDoc.score>=0.1F){

			sentenceMap.put((float) scoreDoc.doc, ">> "+StringUtils.chomp(doc.get("content")));
			//sentenceMap.put(1/scoreDoc.score,sentences.get(docId));
			// System.out.println(scoreDoc);
			// System.out.println(StringUtils.chomp(doc.get("text")));
			// }
		}

		return sentenceMap.values().toArray(new String[0]);

	}
	private static Query computeBooleanQueryNP(String textQuery){
		//List<String> qTokens = new ArrayList<String>();
		//String qText16 = "Formalin fixation has many advantages such as the ease of tissue handling, the possibility of long-term storage, an optimal histological quality and its availability in large quantities at low price.";
		BooleanQuery query = new BooleanQuery();

		HmmDecoder mPosTagger = null;
		TokenizerFactory mTokenizerFactory = null;
		PhraseChunker pChunker = new PhraseChunker(mPosTagger,mTokenizerFactory);
		ArrayList<String> pchuncker = new ArrayList<String>();

		pchuncker = pChunker.getCHunkNounPhrases(textQuery.toLowerCase());
		
		for (int k = 0; k<= pchuncker.size()-1;k++ ){
			String wordInList = ((String) pchuncker.get(k));
			wordInList = wordInList.toLowerCase().replaceAll("-"," ");
			String [] wordInListSplit = wordInList.split(" ");
			for (String word: wordInListSplit){

				query.add(new TermQuery(new Term("content", word)), Occur.SHOULD );
			}
		
		}
		
		/* String[] tQuery = textQuery.split(" ");
		 for(String s: tQuery )
			 query.add(new TermQuery(new Term("text", s)), Occur.SHOULD);*/
		
		return query;
	}

}
