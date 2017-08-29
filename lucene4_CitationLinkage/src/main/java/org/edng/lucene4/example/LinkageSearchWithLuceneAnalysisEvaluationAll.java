package org.edng.lucene4.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
import org.apache.lucene.search.similarities.Similarity;
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

public class LinkageSearchWithLuceneAnalysisEvaluationAll {

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

	Similarity similarity;

	public enum Algo {IBS,Default,BM25,DFR,LMJ,LMD};

	//BM25Similarity similarity = new BM25Similarity(1.2f, 100f);

	//DFRSimilarity similarity = new DFRSimilarity(new BasicModelIF(), new AfterEffectL(), new NormalizationH1());

	//LMJelinekMercerSimilarity similarity = new LMJelinekMercerSimilarity(0.01f);

	//LMDirichletSimilarity similarity = new LMDirichletSimilarity();

	public static Algo[] algo = 
		{Algo.IBS,
		Algo.Default,
		Algo.BM25,
		Algo.DFR,
		Algo.LMJ,
		Algo.LMD
		};

	public Similarity getSimilarity(Algo algo){

		switch(algo){
		case IBS:
			IBSimilarity	similarity = new IBSimilarity(new DistributionLL(), new LambdaDF(), new NormalizationH1()); 
		    return similarity;
		case Default:
			DefaultSimilarity similarityd = new DefaultSimilarity(); //Best or second?
		    return similarityd;

		case BM25:
			BM25Similarity similarityb = new BM25Similarity(1.2f, 100f);
			return similarityb;
		case DFR:
			DFRSimilarity	similaritydf = new DFRSimilarity(new BasicModelIF(), new AfterEffectL(), new NormalizationH1());
		    return similaritydf;
		
		case LMJ:
			LMJelinekMercerSimilarity similarityl = new LMJelinekMercerSimilarity(0.01f);
			return similarityl;
		case LMD:
			LMDirichletSimilarity similarityld = new LMDirichletSimilarity();
			return similarityld;
		}
		return similarity;

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

	public static String[] ReadLinesFromFile(String filename)
	{
		ArrayList<String> result = new ArrayList<String>();

		try {
			BufferedReader file = new BufferedReader(new FileReader(filename));
			String content = file.readLine(); 
			while (content!=null)
			{
				result.add(content);
				content = file.readLine();
			}
			file.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result.toArray(new String[result.size()] );
	}

	@SuppressWarnings("deprecation")
	@org.junit.Test
	public void runTest() throws Exception {
		loadStopwords();
		@SuppressWarnings("deprecation")
		CharArraySet stopSet = CharArraySet.copy(StandardAnalyzer.STOP_WORDS_SET);

		stopSet.addAll(stopwords);

		HashMap<String, Integer> arrayLMap = new HashMap<String, Integer>();
		HashMap<String, Double> arrayLMapCandidate= new HashMap<String, Double>();

		List<String> sentences = new ArrayList<String>();
		@SuppressWarnings("deprecation")
		StandardAnalyzer analyzer = new StandardAnalyzer(stopSet);
		//SnowballAnalyzer  sba = new SnowballAnalyzer(Version.LATEST, "English");

		//PayloadAnalyzer analyzer = new PayloadAnalyzer();

		//  TextField textField = new TextField("content", "", Field.Store.YES);

		String fileName = "C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/annotationdata1ModifiedBSRed.txt";
		String textFromFile = Files.readFromFile(new File(fileName),"ISO-8859-1");
		String[] listFromFile = textFromFile.split("\n");

		/*String fileName = "C:/Users/Hospice/Downloads/Thesis/annotateSarah3/sarahfiles/annotationdata1.txt";
		String textFromFile = Files.readFromFile(new File(fileName),"ISO-8859-1");

		double wNDCGValAg = 0;
		String[] listFromFile = textFromFile.split("\n");*/
		//String dir = "C:/Users/Hospice/Documents/boruta_data/linkageText/newfilesforrteMultiTruth/";
		//String dir = "C:/Users/Hospice/Documents/boruta_data/linkageText/dataperfilePart/";

		//File pdfFile = new File(dir);
		//String[] filesPath;
		// array of files and directory
		//filesPath = pdfFile.list();
		int t = 0;
		//String dirF = "C:/Users/Hospice/Documents/boruta_data/linkageText/dataperfileIRPart/";
			//BufferedWriter bw = new BufferedWriter(new FileWriter(dirF+file));
			//System.out.println(file);
			//for(Algo sim:algo){
			for(int k = 0; k < 1; k++){
				ArrayList precisions = new ArrayList();
				ArrayList precisionsnDCG = new ArrayList();
				for(String line:listFromFile){	

					IBSimilarity	similarity = new IBSimilarity(new DistributionLL(), new LambdaDF(), new NormalizationH1()); 

				//BufferedWriter bw = new BufferedWriter(new FileWriter("C:/Users/Hospice/combinesentences/filesforrteMultiTruthWithMethod/1471-2105-10-434_sent.txt"));
				IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);

				//config.setSimilarity(getSimilarity(sim));
				config.setSimilarity(similarity);


				//IBSimilarity similarity = new IBSimilarity(new DistributionLL(), new LambdaDF(), new NormalizationH1());  //Best?

				//DefaultSimilarity similarity = new DefaultSimilarity(); //Best or second?

				//BM25Similarity similarity = new BM25Similarity(1.2f, 100f);


				//DFRSimilarity similarity = new DFRSimilarity(new BasicModelIF(), new AfterEffectL(), new NormalizationH1());

				//LMJelinekMercerSimilarity similarity = new LMJelinekMercerSimilarity(0.01f);

				//LMDirichletSimilarity similarity = new LMDirichletSimilarity();

				//for(Algo sim:algo){
				//	config.setSimilarity(getSimilarity(sim));
				//}

				//config.setSimilarity(getSimilarity(algo[1]));

				Document doc = new Document();

				RAMDirectory ramdir = new RAMDirectory();
				IndexWriter indexWriter = new IndexWriter(ramdir, config);
				String fileL = line.split("\t")[0];
				System.out.println(fileL);
				String[] sentencesL = ReadLinesFromFile("C:/Users/Hospice/combinesentences/filesforrteMultiTruthWithMethod/"+fileL);

				System.out.println(sentencesL[0]);
				String citationText = sentencesL[0].split("\t")[0];
				
				for(int i=0; i < sentencesL.length-1; i++){
					String lineL[] = sentencesL[i].split("\t");
					Document doc2 = new Document();
                    if(lineL.length!=0){
					String textL = lineL[1];
                    
					//List of sentences
					arrayLMap.put(textL, Integer.valueOf(lineL[2]));
                    
					if(Integer.valueOf(lineL[2])>0)
					{
						arrayLMapCandidate.put(textL, (double)Integer.valueOf(lineL[2]));
					}
					//System.out.println(textL);
					doc2.add(new Field("content",textL, Store.YES, Index.ANALYZED));
					//System.out.println(sentences.get(i));

					indexWriter.addDocument(doc2);
                    }

				}
				indexWriter.commit();
				int count = 0;
				IndexReader indexReader = DirectoryReader.open(ramdir);
				IndexSearcher indexSearcher = new IndexSearcher(indexReader);

				indexSearcher.setSimilarity(similarity);
				BooleanQuery booleanQuery = new BooleanQuery();
				String [] wordInListSplit = citationText.toLowerCase().split(" ");
				for (String word: wordInListSplit){

					booleanQuery.add(new TermQuery(new Term("content", word)), Occur.SHOULD );
				}

				TopDocs topDocs = indexSearcher.search(booleanQuery, 18);
				HashMap<String, Double> hashMapFromRanking = new HashMap<String, Double>();

				for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
					//System.out.println( query.toString("content"));
					// System.out.println(scoreDoc.score);
					// System.out.println(scoreDoc.score);
					int docId = scoreDoc.doc;
					
					//indexSearcher.explain(query, docId);

					//System.out.println(indexSearcher.explain(query, docId));

					//System.out.println(indexSearcher.collectionStatistics("content"));
					doc = indexSearcher.doc(docId);
					
					System.out.println(arrayLMap.get(doc.get("content"))+","+ doc.get("content"));
				
					if(arrayLMap.get(doc.get("content"))>0){
						hashMapFromRanking.put(doc.get("content"), (double)arrayLMap.get(doc.get("content")));
						count++;
					}
					//System.out.println(sentences.get(docId));
					//if (scoreDoc.score>=0)
					//System.out.println(scoreDoc.score+","+arrayLMap.get(doc.get("content"))+","+ doc.get("content"));
					
					//StringBuilder sb = new StringBuilder();

					//sb.append(scoreDoc.score+","+arrayLMap.get(doc.get("content"))+","+ doc.get("content").split("	")[1]+"\n");
					//bw.write(sb.toString());
				}
				WeightedNormalizedDiscountedCumulativeGain  wNDCG = new WeightedNormalizedDiscountedCumulativeGain();

				double wNDCGValue = 
						wNDCG.evaluateInstance(arrayLMapCandidate, hashMapFromRanking, 0);
				//wNDCGValAg+=wNDCGValue;
				System.out.println("Precision sentences # :"+ count);
				//output.write("Precision at " +listCandidate.size() +" :"  +(double)count/precisionNumber+"\n"+"\n");
				System.out.println("Precision at " +arrayLMapCandidate.size() +" :"  +(double)count/arrayLMapCandidate.size());
				precisions.add((double)count/arrayLMapCandidate.size());
				System.out.println("WNCG: " +wNDCGValue);
				precisionsnDCG.add(wNDCGValue);
				//bw.write("\n");
				//bw.close();
			}
			System.out.println(precisions);
			System.out.println(precisionsnDCG);


			//bw.close();
		}

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

			// System.out.println(scoreDoc.score)
			int docId = scoreDoc.doc;
			//System.out.println(searcher.explain(query, docId));
			Document doc = indexSearcher.doc(docId);
			// System.out.println(sentences.get(docId));
			System.out.println(doc.get("content"));

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
