package org.edng.lucene4.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
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
import stemmer.StanfordLemmatizer;

public class LinkageSearchWithLuceneAnalysisEvaluationPerPaper {

	//private int numSentences;
	//private static double score = 0;
	//private static int number = 0;
	private String textSentenceL = "";
	public static Set<String> stopwords = new HashSet<String>();
	public String outputRankedText =  "";
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

		int numSentences;
		StanfordLemmatizer lematizer = new StanfordLemmatizer();


		DecimalFormat df = new DecimalFormat("#.####");

		loadStopwords();
		@SuppressWarnings("deprecation")
		CharArraySet stopSet = CharArraySet.copy(StandardAnalyzer.STOP_WORDS_SET);

		stopSet.addAll(stopwords);
		BufferedWriter bw = new BufferedWriter(new FileWriter("C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/IR_23_11/"+"evalPrec_.txt"));
		BufferedWriter bwdc = new BufferedWriter(new FileWriter("C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/IR_23_11/"+"evaldc_.txt"));
		BufferedWriter bwScore = new BufferedWriter(new FileWriter("C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/IR_23_11/"+"outScoreb_.txt"));
		BufferedWriter ExbwScore = new BufferedWriter(new FileWriter("C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/IR_23_11/"+"expScore_.txt"));

		BufferedWriter scoreAnalysis = new BufferedWriter(new FileWriter("C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/IR_22_11_M/"+"analysisScore_.txt"));


		List<String> sentences = new ArrayList<String>();
		@SuppressWarnings("deprecation")
		StandardAnalyzer analyzer = new StandardAnalyzer(stopSet);
		//SnowballAnalyzer  sba = new SnowballAnalyzer(Version.LATEST, "English");

		//PayloadAnalyzer analyzer = new PayloadAnalyzer();

		//  TextField textField = new TextField("content", "", Field.Store.YES);

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
		//				   C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/annotationdataBSAnalysis.txt
		//String fileName = "C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/annotationdata1ModifiedBSRed.txt";

		String fileName = "C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/annotationdataBSAnalysis.txt";


		String textFromFile = Files.readFromFile(new File(fileName),"ISO-8859-1");
		String[] listFromFile = textFromFile.split("\n");
		int numPaper = listFromFile.length;


		//String dirF = "C:/Users/Hospice/Documents/boruta_data/linkageText/dataperfileIRPart/";
		//for(String file:filesPath){	
		//BufferedWriter bw = new BufferedWriter(new FileWriter(dirF+file));
		//System.out.println(file);
		//for(Algo sim:algo){
		//IBSimilarity	similarity = new IBSimilarity(new DistributionLL(), new LambdaDF(), new NormalizationH1()); 
		for(String fileS:listFromFile){
			ArrayList precision = new ArrayList();
			ArrayList<Double> precisionsnDCG = new ArrayList<Double>();

			for(int k = 0; k < algo.length; k++){
				double wNDCGValAg = 0;
				double score = 0;
				//int number = 0;
				int t = 0;
				bwScore.write(algo[k].toString()+"\n");
				ExbwScore.write(algo[k].toString()+"\n");
				bwScore.write("\\hline"+"\n");
				bwScore.write("Paper #" + " & " +"# of sentences" + " & "+  "precision # (k)" + " & " + "precision@k"+ " & " +"wNDCG@k"+"\\\\"+"\n");
				bwScore.write("\\hline"+"\n");
				//ArrayList precision = new ArrayList();
				//ArrayList<Double> precisionsnDCG = new ArrayList<Double>();
				//for(String fileS:listFromFile){
				t++;		
				HashMap<String, Integer> arrayLMap = new HashMap<String, Integer>();
				HashMap<String, Double> arrayLMapCandidate= new HashMap<String, Double>();
				//BufferedWriter bw = new BufferedWriter(new FileWriter("C:/Users/Hospice/combinesentences/filesforrteMultiTruthWithMethod/1471-2105-10-434_sent.txt"));
				IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
				//config.setSimilarity(getSimilarity(sim));
				config.setSimilarity(getSimilarity(algo[k]));
				Document doc = new Document();
				RAMDirectory ramdir = new RAMDirectory();
				IndexWriter indexWriter = new IndexWriter(ramdir, config);
				//String[] sentencesL = ReadLinesFromFile("C:/Users/Hospice/combinesentences/filesforrteMultiTruthWithMethod/1471-2105-10-434_sent.txt");
				//String[] sentencesL = ReadLinesFromFile("C:/Users/Hospice/combinesentences/filesforrteMultiTruthWithMethod/"+fileS.split("\t")[0]);
				//String[] sentencesL = ReadLinesFromFile("C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/newfilesforrteMultiTruthOutAllCandidateMethod2/"+fileS.split("\t")[0]);

				String[] sentencesL = ReadLinesFromFile("C:/Users/Hospice/Documents/boruta_data/linkageText/newfilesforrteMultiTruth/"+fileS.split("\t")[0]);

				//System.out.println(t + "\t"+fileS.split("\t")[0]);
				//String citationText = sentencesL[0].split("\t")[0];

				String citationText = lematizer.lemmatizeText(fileS.split("\t")[1]);
				ExbwScore.write(citationText+"\n");

				for(int i=0; i < sentencesL.length; i++){
					if(sentencesL[i].length()!=0){
						String lineL[] = sentencesL[i].split("\t");
						//Document doc2 = new Document();

						String textL = lineL[1];
						//List of sentences
						arrayLMap.put(lematizer.lemmatizeText(textL), Integer.valueOf(lineL[2]));
						if(Integer.valueOf(lineL[2])>0)
						{
							arrayLMapCandidate.put(lematizer.lemmatizeText(textL), (double)Integer.valueOf(lineL[2]));
						}				
					}
				}

				for(int i=0; i < sentencesL.length; i++){
					//if(sentencesL[i].length()!=0){
					String lineL[] = sentencesL[i].split("\t");
					Document doc2 = new Document();

					String textL = lineL[1];
					//if (textL.split(" ").length > 4)
					//List of sentences
					arrayLMap.put(lematizer.lemmatizeText(textL), Integer.valueOf(lineL[2]));
					/*if(Integer.valueOf(lineL[2])>0)
					{
						arrayLMapCandidate.put(textL, (double)Integer.valueOf(lineL[2]));
					}*/
					//System.out.println(textL);
					doc2.add(new Field("content",lematizer.lemmatizeText(textL), Store.YES, Index.ANALYZED));
					//System.out.println(sentences.get(i));

					indexWriter.addDocument(doc2);
					//}

				}
				indexWriter.commit();
				int count = 0;
				IndexReader indexReader = DirectoryReader.open(ramdir);
				IndexSearcher indexSearcher = new IndexSearcher(indexReader);
				indexSearcher.setSimilarity(getSimilarity(algo[k]));
				BooleanQuery booleanQuery = new BooleanQuery();
				String [] wordInListSplit = citationText.toLowerCase().split(" ");
				for (String word: wordInListSplit){

					booleanQuery.add(new TermQuery(new Term("content", word)), Occur.SHOULD );
				}
				System.out.println(booleanQuery.clauses());
				BooleanQuery bQuery = new BooleanQuery();

				bQuery = (BooleanQuery) computeBooleanQueryNP(citationText.replace("[~%()]", " "));
				System.out.println(citationText+"\n");
				System.out.println(bQuery.clauses());			

				if(arrayLMapCandidate.size()>0){
					TopDocs topDocs = indexSearcher.search(booleanQuery, arrayLMapCandidate.size());
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

						ExbwScore.write(arrayLMap.get(doc.get("content"))+","+ doc.get("content")+"\n");
						scoreAnalysis.write(arrayLMap.get(doc.get("content"))+","+ doc.get("content")+"\n");

						//System.out.println(indexSearcher.explain(booleanQuery, docId));
						ExbwScore.write(indexSearcher.explain(booleanQuery, docId).toString()+"\n");
						if(arrayLMap.get(doc.get("content"))>0){ //Check if document is a linkage candidate
							hashMapFromRanking.put(doc.get("content"), (double)arrayLMap.get(doc.get("content")));
							count++;
						}
						//System.out.println(sentences.get(docId));

					}
					WeightedNormalizedDiscountedCumulativeGain  wNDCG = new WeightedNormalizedDiscountedCumulativeGain();

					//if(arrayLMapCandidate.size()>0){
					double wNDCGValue = 
							wNDCG.evaluateInstance(arrayLMapCandidate, hashMapFromRanking, 0);
					wNDCGValAg+=wNDCGValue;
					System.out.println("Precision sentences # :"+ count);
					//output.write("Precision at " +listCandidate.size() +" :"  +(double)count/precisionNumber+"\n"+"\n");
					System.out.println("Precision at " +arrayLMapCandidate.size() +" :"  +(double)count/arrayLMapCandidate.size());
					precision.add((double)count/arrayLMapCandidate.size());
					precisionsnDCG.add(wNDCGValue);
					System.out.println("WNCG: " +wNDCGValue);
					double linkageScore = (double)count/arrayLMapCandidate.size();
					score += linkageScore;
					//bw.write("\n");
					//bw.close();
					//number++;
					String out = t + " & " +sentencesL.length + " & "+  arrayLMapCandidate.size() + " & " + df.format(linkageScore)+ " & " +df.format(wNDCGValue)+"\\\\"+"\n";
					bwScore.write(out);
					//ExbwScore.write(" ****** End Paper "+t +"*********\n" );
					//scoreAnalysis.write(" ****** End Paper "+t +"*********\n\n" );

				} // partial evaluation

			} // end search algos
			System.out.println(precision);
			System.out.println(precisionsnDCG);
			System.out.println();
			//bwScore.write("\n");
			//output.write("Number of valid linkages  " + number+"\n");
			//System.out.println("Number of valid linkages  " + numPaper);
			//output.write("Overrall Precision Score  " + score/number);
			//double prec = score/numPaper;
			//	System.out.println("Overrall Precision Score  " + score/numPaper);

			//System.out.println("Overrall wNDCG Score  " +wNDCGValAg/numPaper);
			//double wNDCGVal = wNDCGValAg/numPaper;
			//bw.close();
			//}
			//System.out.println(numPaper);
			//System.out.println(precision);
			//bw.write(algo[k].toString()+"_Prec"+"<-"+precision.toString().replace("[", "c(").replace("]", ")")+"\n\n");
			//System.out.println(precisionsnDCG);
			//bwdc.write(algo[k].toString()+"nDCG"+"<-"+precisionsnDCG.toString().replace("[", "c(").replace("]", ")")+"\n\n");
			//bwScore.write(""+prec+"\n");
			//bwScore.write(""+wNDCGVal+"\n\n");		
			//ExbwScore.write("============================\n\n");
			//scoreAnalysis.write("============================\n\n");

		}// end paper
		bw.close();
		bwdc.close();
		bwScore.close();
		ExbwScore.close();
		scoreAnalysis.close();
	}

	public ArrayList<Double> computeEvalWithoutStem(String fileS, String filePath) throws Exception {
		outputRankedText+= "****** Without Stem ******"+"\n";
		int numSentences;
		StanfordLemmatizer lematizer = new StanfordLemmatizer();

		DecimalFormat df = new DecimalFormat("#.####");

		loadStopwords();
		@SuppressWarnings("deprecation")
		CharArraySet stopSet = CharArraySet.copy(StandardAnalyzer.STOP_WORDS_SET);
		stopSet.addAll(stopwords);		
		List<String> sentences = new ArrayList<String>();
		@SuppressWarnings("deprecation")
		StandardAnalyzer analyzer = new StandardAnalyzer(stopSet);
		ArrayList precision = new ArrayList();
		ArrayList<Double> precisionsnDCG = new ArrayList<Double>();		
		for(int k = 0; k < algo.length; k++){
			double wNDCGValAg = 0;
			double score = 0;
			//int number = 0;
			int t = 0;

			t++;		
			HashMap<String, Integer> arrayLMap = new HashMap<String, Integer>();
			HashMap<String, Double> arrayLMapCandidate= new HashMap<String, Double>();
			//BufferedWriter bw = new BufferedWriter(new FileWriter("C:/Users/Hospice/combinesentences/filesforrteMultiTruthWithMethod/1471-2105-10-434_sent.txt"));
			IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
			//config.setSimilarity(getSimilarity(sim));
			outputRankedText+= "Similarity Algo :" +algo[k]+"\n";
			System.out.println("Similarity Algo :" +algo[k]);
			config.setSimilarity(getSimilarity(algo[k]));
			Document doc = new Document();
			RAMDirectory ramdir = new RAMDirectory();
			IndexWriter indexWriter = new IndexWriter(ramdir, config);

			//String[] sentencesL = ReadLinesFromFile("C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/newfilesforrteMultiTruthOutAllCandidateMethod/"+fileS.split("\t")[0]);
			String[] sentencesL = ReadLinesFromFile(filePath+fileS.split("\t")[0]);

			//filePath

			//String[] sentencesL = ReadLinesFromFile("C:/Users/Hospice/Documents/boruta_data/linkageText/newfilesforrteMultiTruth/"+fileS.split("\t")[0]);

			String citeText = fileS.split("\t")[1];
			String npQurey = citeText.replaceAll("-"," ");

			String citationText = "";

			citationText = lematizer.lemmatizeText(npQurey);

			for(int i=0; i < sentencesL.length; i++){
				if(sentencesL[i].length()!=0){
					String lineL[] = sentencesL[i].split("\t");
					//Document doc2 = new Document();

					String textL = lineL[1];
					//String npSenence = computeNP(textL);
					//List of sentences
					//arrayLMap.put(lematizer.lemmatizeText(textL), Integer.valueOf(lineL[2]));
					if(Integer.valueOf(lineL[2])>0)
					{
						arrayLMapCandidate.put(lematizer.lemmatizeText(textL), (double)Integer.valueOf(lineL[2]));
					}				
				}
			}

			for(int i=0; i < sentencesL.length; i++){
				//if(sentencesL[i].length()!=0){
				String lineL[] = sentencesL[i].split("\t");
				Document doc2 = new Document();
				String textL = lineL[1];
				//String npSenence = computeNP(textL);

				//if (textL.split(" ").length > 4)
				//List of sentences
				arrayLMap.put(lematizer.lemmatizeText(textL), Integer.valueOf(lineL[2]));
				/*if(Integer.valueOf(lineL[2])>0)
					{
						arrayLMapCandidate.put(textL, (double)Integer.valueOf(lineL[2]));
					}*/
				//System.out.println(textL);
				doc2.add(new Field("content",lematizer.lemmatizeText(textL), Store.YES, Index.ANALYZED));
				//System.out.println(sentences.get(i));

				indexWriter.addDocument(doc2);
				//}

			}
			indexWriter.commit();
			int count = 0;
			IndexReader indexReader = DirectoryReader.open(ramdir);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			indexSearcher.setSimilarity(getSimilarity(algo[k]));
			BooleanQuery booleanQuery = new BooleanQuery();
			String [] wordInListSplit = citationText.toLowerCase().split(" ");
			for (String word: wordInListSplit){

				booleanQuery.add(new TermQuery(new Term("content", word.trim())), Occur.SHOULD );
			}
			//System.out.println(booleanQuery.clauses());
			//BooleanQuery bQuery = new BooleanQuery();

			//bQuery = (BooleanQuery) computeBooleanQueryNP(citationText.replace("[~%()]", " "));
			//System.out.println(citationText+"\n");
			//System.out.println(bQuery.clauses());			

			if(arrayLMapCandidate.size()>0){
				TopDocs topDocs = indexSearcher.search(booleanQuery, 10);
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

					System.out.println(arrayLMap.get(doc.get("content"))+", "+scoreDoc.score+", "+ doc.get("content"));				
					outputRankedText+=arrayLMap.get(doc.get("content"))+", "+scoreDoc.score+", "+ doc.get("content")+"\n";

					//System.out.println(indexSearcher.explain(booleanQuery, docId));
					if(arrayLMap.get(doc.get("content"))>0){ //Check if document is a linkage candidate
						hashMapFromRanking.put(doc.get("content"), (double)arrayLMap.get(doc.get("content")));
						count++;
					}

				}
				WeightedNormalizedDiscountedCumulativeGain  wNDCG = new WeightedNormalizedDiscountedCumulativeGain();

				//if(arrayLMapCandidate.size()>0){
				double wNDCGValue = 
						wNDCG.evaluateInstance(arrayLMapCandidate, hashMapFromRanking, 0);
				wNDCGValAg+=wNDCGValue;
				System.out.println("Precision sentences # :"+ count);
				//output.write("Precision at " +listCandidate.size() +" :"  +(double)count/precisionNumber+"\n"+"\n");
				System.out.println("Precision at " +arrayLMapCandidate.size() +" :"  +(double)count/arrayLMapCandidate.size());
				precision.add((double)count/arrayLMapCandidate.size());
				precisionsnDCG.add(wNDCGValue);
				System.out.println("WNCG: " +wNDCGValue);
				double linkageScore = (double)count/arrayLMapCandidate.size();
				score += linkageScore;


			} // partial evaluation

		} // end search algos
		//System.out.println(precision);
		//System.out.println(precisionsnDCG);
		System.out.println();

		return precisionsnDCG;
	}


	public ArrayList<Double> computeEvalWithStem(String fileS, String filePath) throws Exception {
		outputRankedText+= "******  With Stem ****** "+"\n";
		int numSentences;
		StanfordLemmatizer lematizer = new StanfordLemmatizer();
		DecimalFormat df = new DecimalFormat("#.####");
		loadStopwords();
		@SuppressWarnings("deprecation")
		CharArraySet stopSet = CharArraySet.copy(StandardAnalyzer.STOP_WORDS_SET);
		stopSet.addAll(stopwords);		
		List<String> sentences = new ArrayList<String>();
		@SuppressWarnings("deprecation")
		StandardAnalyzer analyzer = new StandardAnalyzer(stopSet);
		ArrayList precision = new ArrayList();
		ArrayList<Double> precisionsnDCG = new ArrayList<Double>();		
		for(int k = 0; k < algo.length; k++){
			double wNDCGValAg = 0;
			double score = 0;
			//int number = 0;
			int t = 0;

			t++;		
			HashMap<String, Integer> arrayLMap = new HashMap<String, Integer>();
			HashMap<String, Double> arrayLMapCandidate= new HashMap<String, Double>();
			//BufferedWriter bw = new BufferedWriter(new FileWriter("C:/Users/Hospice/combinesentences/filesforrteMultiTruthWithMethod/1471-2105-10-434_sent.txt"));
			IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
			//config.setSimilarity(getSimilarity(sim));
			outputRankedText+= "Similarity Algo :" +algo[k]+"\n";
			System.out.println("Similarity Algo :" +algo[k]);

			config.setSimilarity(getSimilarity(algo[k]));
			Document doc = new Document();
			RAMDirectory ramdir = new RAMDirectory();
			IndexWriter indexWriter = new IndexWriter(ramdir, config);	

			String[] sentencesL = ReadLinesFromFile(filePath+fileS.split("\t")[0]);

			//String[] sentencesL = ReadLinesFromFile("C:/Users/Hospice/Documents/boruta_data/linkageText/newfilesforrteMultiTruth/"+fileS.split("\t")[0]);
			String citeText = fileS.split("\t")[1];
			String npQurey = citeText.replaceAll("-"," ");

			System.out.println(npQurey);

			String citationText = "";

			citationText = lematizer.lemmatizeStemText(npQurey);

			//String citationText = lematizer.lemmatizeStemText(fileS.split("\t")[1]);

			for(int i=0; i < sentencesL.length; i++){
				if(sentencesL[i].length()!=0){
					String lineL[] = sentencesL[i].split("\t");
					//Document doc2 = new Document();

					String textL = lineL[1];
					//String npSenence = computeNP(textL);

					//List of sentences
					//arrayLMap.put(lematizer.lemmatizeStemText(npSenence), Integer.valueOf(lineL[2]));
					if(Integer.valueOf(lineL[2])>0)
					{
						arrayLMapCandidate.put(lematizer.lemmatizeStemText(textL), (double)Integer.valueOf(lineL[2]));
					}				
				}
			}

			for(int i=0; i < sentencesL.length; i++){
				//if(sentencesL[i].length()!=0){
				String lineL[] = sentencesL[i].split("\t");
				Document doc2 = new Document();
				String textL = lineL[1];
				//String npSenence = computeNP(textL);

				arrayLMap.put(lematizer.lemmatizeStemText(textL), Integer.valueOf(lineL[2]));

				doc2.add(new Field("content",lematizer.lemmatizeStemText(textL), Store.YES, Index.ANALYZED));
				//System.out.println(sentences.get(i));

				indexWriter.addDocument(doc2);
				//}

			}
			indexWriter.commit();
			int count = 0;
			IndexReader indexReader = DirectoryReader.open(ramdir);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			indexSearcher.setSimilarity(getSimilarity(algo[k]));
			BooleanQuery booleanQuery = new BooleanQuery();
			String [] wordInListSplit = citationText.toLowerCase().split(" ");
			for (String word: wordInListSplit){

				booleanQuery.add(new TermQuery(new Term("content", word.trim())), Occur.SHOULD );
			}
			//System.out.println(booleanQuery.clauses());
			//BooleanQuery bQuery = new BooleanQuery();

			//bQuery = (BooleanQuery) computeBooleanQueryNP(citationText.replace("[~%()]", " "));
			//System.out.println(citationText+"\n");
			//System.out.println(bQuery.clauses());			

			if(arrayLMapCandidate.size()>0){
				TopDocs topDocs = indexSearcher.search(booleanQuery, 10);
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

					//System.out.println(arrayLMap.get(doc.get("content"))+","+ doc.get("content"));				

					System.out.println(arrayLMap.get(doc.get("content"))+", "+scoreDoc.score+", "+ doc.get("content"));				
					outputRankedText+=arrayLMap.get(doc.get("content"))+", "+scoreDoc.score+", "+ doc.get("content")+"\n";

					//System.out.println(indexSearcher.explain(booleanQuery, docId));
					if(arrayLMap.get(doc.get("content"))>0){ //Check if document is a linkage candidate
						hashMapFromRanking.put(doc.get("content"), (double)arrayLMap.get(doc.get("content")));
						count++;
					}

				}
				WeightedNormalizedDiscountedCumulativeGain  wNDCG = new WeightedNormalizedDiscountedCumulativeGain();

				//if(arrayLMapCandidate.size()>0){
				double wNDCGValue = 
						wNDCG.evaluateInstance(arrayLMapCandidate, hashMapFromRanking, 0);
				wNDCGValAg+=wNDCGValue;
				System.out.println("Precision sentences # :"+ count);
				//output.write("Precision at " +listCandidate.size() +" :"  +(double)count/precisionNumber+"\n"+"\n");
				System.out.println("Precision at " +arrayLMapCandidate.size() +" :"  +(double)count/arrayLMapCandidate.size());
				precision.add((double)count/arrayLMapCandidate.size());
				precisionsnDCG.add(wNDCGValue);
				System.out.println("WNCG: " +wNDCGValue);
				double linkageScore = (double)count/arrayLMapCandidate.size();
				score += linkageScore;


			} // partial evaluation

		} // end search algos
		//System.out.println(precision);
		//System.out.println(precisionsnDCG);
		System.out.println();

		return precisionsnDCG;
	}

	public ArrayList<Double> computeEvalBaseline(String fileS, String filePath) throws Exception {
		outputRankedText+= "******  Baseline ******  "+"\n";

		int numSentences;
		StanfordLemmatizer lematizer = new StanfordLemmatizer();
		DecimalFormat df = new DecimalFormat("#.####");
		loadStopwords();
		@SuppressWarnings("deprecation")
		CharArraySet stopSet = CharArraySet.copy(StandardAnalyzer.STOP_WORDS_SET);
		stopSet.addAll(stopwords);		
		List<String> sentences = new ArrayList<String>();
		@SuppressWarnings("deprecation")
		StandardAnalyzer analyzer = new StandardAnalyzer(stopSet);
		ArrayList precision = new ArrayList();
		ArrayList<Double> precisionsnDCG = new ArrayList<Double>();		
		for(int k = 0; k < algo.length; k++){
			double wNDCGValAg = 0;
			double score = 0;
			//int number = 0;
			int t = 0;

			t++;		
			HashMap<String, Integer> arrayLMap = new HashMap<String, Integer>();
			HashMap<String, Double> arrayLMapCandidate= new HashMap<String, Double>();
			//BufferedWriter bw = new BufferedWriter(new FileWriter("C:/Users/Hospice/combinesentences/filesforrteMultiTruthWithMethod/1471-2105-10-434_sent.txt"));
			IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
			//config.setSimilarity(getSimilarity(sim));
			outputRankedText+= "Similarity Algo :" +algo[k]+"\n";
			System.out.println("Similarity Algo :" +algo[k]);

			config.setSimilarity(getSimilarity(algo[k]));
			Document doc = new Document();
			RAMDirectory ramdir = new RAMDirectory();
			IndexWriter indexWriter = new IndexWriter(ramdir, config);	

			String[] sentencesL = ReadLinesFromFile(filePath+fileS.split("\t")[0]);


			//String[] sentencesL = ReadLinesFromFile("C:/Users/Hospice/Documents/boruta_data/linkageText/newfilesforrteMultiTruth/"+fileS.split("\t")[0]);
			String citeText = fileS.split("\t")[1];
			String npQurey = citeText.replaceAll("-"," ");

			System.out.println(npQurey);

			String citationText = "";

			citationText = npQurey;

			//String citationText = lematizer.lemmatizeStemText(fileS.split("\t")[1]);

			for(int i=0; i < sentencesL.length; i++){
				if(sentencesL[i].length()!=0){
					String lineL[] = sentencesL[i].split("\t");
					//Document doc2 = new Document();

					String textL = lineL[1];
					//String npSenence = computeNP(textL);

					//List of sentences
					//arrayLMap.put(lematizer.lemmatizeStemText(npSenence), Integer.valueOf(lineL[2]));
					if(Integer.valueOf(lineL[2])>0)
					{
						arrayLMapCandidate.put(textL, (double)Integer.valueOf(lineL[2]));
					}				
				}
			}

			for(int i=0; i < sentencesL.length; i++){
				//if(sentencesL[i].length()!=0){
				String lineL[] = sentencesL[i].split("\t");
				Document doc2 = new Document();
				String textL = lineL[1];
				//String npSenence = computeNP(textL);

				arrayLMap.put(textL, Integer.valueOf(lineL[2]));

				doc2.add(new Field("content",textL, Store.YES, Index.ANALYZED));
				//System.out.println(sentences.get(i));

				indexWriter.addDocument(doc2);
				//}

			}
			indexWriter.commit();
			int count = 0;
			IndexReader indexReader = DirectoryReader.open(ramdir);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			indexSearcher.setSimilarity(getSimilarity(algo[k]));
			BooleanQuery booleanQuery = new BooleanQuery();
			String [] wordInListSplit = citationText.toLowerCase().split(" ");
			for (String word: wordInListSplit){

				booleanQuery.add(new TermQuery(new Term("content", word.trim())), Occur.SHOULD );
			}
			//System.out.println(booleanQuery.clauses());
			//BooleanQuery bQuery = new BooleanQuery();

			//bQuery = (BooleanQuery) computeBooleanQueryNP(citationText.replace("[~%()]", " "));
			//System.out.println(citationText+"\n");
			//System.out.println(bQuery.clauses());			

			if(arrayLMapCandidate.size()>0){
				TopDocs topDocs = indexSearcher.search(booleanQuery, 10);
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

					//System.out.println(arrayLMap.get(doc.get("content"))+","+ doc.get("content"));				

					System.out.println(arrayLMap.get(doc.get("content"))+", "+scoreDoc.score+", "+ doc.get("content"));				

					outputRankedText+= arrayLMap.get(doc.get("content"))+", "+scoreDoc.score+", "+ doc.get("content")+"\n";

					//System.out.println(indexSearcher.explain(booleanQuery, docId));
					if(arrayLMap.get(doc.get("content"))>0){ //Check if document is a linkage candidate
						hashMapFromRanking.put(doc.get("content"), (double)arrayLMap.get(doc.get("content")));
						count++;
					}

				}
				WeightedNormalizedDiscountedCumulativeGain  wNDCG = new WeightedNormalizedDiscountedCumulativeGain();

				//if(arrayLMapCandidate.size()>0){
				double wNDCGValue = 
						wNDCG.evaluateInstance(arrayLMapCandidate, hashMapFromRanking, 0);
				wNDCGValAg+=wNDCGValue;
				System.out.println("Precision sentences # :"+ count);
				//output.write("Precision at " +listCandidate.size() +" :"  +(double)count/precisionNumber+"\n"+"\n");
				System.out.println("Precision at " +arrayLMapCandidate.size() +" :"  +(double)count/arrayLMapCandidate.size());
				precision.add((double)count/arrayLMapCandidate.size());
				precisionsnDCG.add(wNDCGValue);
				System.out.println("WNCG: " +wNDCGValue);
				double linkageScore = (double)count/arrayLMapCandidate.size();
				score += linkageScore;


			} // partial evaluation
			outputRankedText+="\n";
		} // end search algos
		//System.out.println(precision);
		//System.out.println(precisionsnDCG);
		System.out.println();

		return precisionsnDCG;
	}



	public ArrayList<Double> computeEvaluation(String fileS, String filePath, int exp) throws Exception {

		int numSentences;
		StanfordLemmatizer lematizer = new StanfordLemmatizer();
		DecimalFormat df = new DecimalFormat("#.####");
		loadStopwords();
		@SuppressWarnings("deprecation")
		CharArraySet stopSet = CharArraySet.copy(StandardAnalyzer.STOP_WORDS_SET);
		stopSet.addAll(stopwords);		
		List<String> sentences = new ArrayList<String>();
		@SuppressWarnings("deprecation")
		StandardAnalyzer analyzer = new StandardAnalyzer(stopSet);
		ArrayList precision = new ArrayList();
		ArrayList<Double> precisionsnDCG = new ArrayList<Double>();		
		for(int k = 0; k < algo.length; k++){
			double wNDCGValAg = 0;
			double score = 0;
			//int number = 0;
			int t = 0;
			t++;		
			HashMap<String, Integer> arrayLMap = new HashMap<String, Integer>();
			HashMap<String, Double> arrayLMapCandidate= new HashMap<String, Double>();
			//BufferedWriter bw = new BufferedWriter(new FileWriter("C:/Users/Hospice/combinesentences/filesforrteMultiTruthWithMethod/1471-2105-10-434_sent.txt"));
			IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
			//config.setSimilarity(getSimilarity(sim));
			System.out.println("Similarity Algo :" +algo[k]);

			config.setSimilarity(getSimilarity(algo[k]));
			Document doc = new Document();
			RAMDirectory ramdir = new RAMDirectory();
			IndexWriter indexWriter = new IndexWriter(ramdir, config);	

			String[] sentencesL = ReadLinesFromFile(filePath+fileS.split("\t")[0]);
			String citeText ="";
			
			//System.out.println(sentencesL.length);
			String npQurey = "";

			//System.out.println(npQurey);

			String citationText = "";

			//

			//String citationText = lematizer.lemmatizeStemText(fileS.split("\t")[1]);
			switch(exp){
			case 0:
				citeText = fileS.split("\t")[1].replaceAll("-"," ");
				npQurey = citeText;
				citationText = npQurey;
				for(int i=0; i < sentencesL.length; i++){
					if(sentencesL[i].length()!=0){
						String lineL[] = sentencesL[i].split("\t");
						//Document doc2 = new Document();

						String textL = lineL[1];
						//String npSenence = computeNP(textL);

						//List of sentences
						//arrayLMap.put(lematizer.lemmatizeStemText(npSenence), Integer.valueOf(lineL[2]));
						if(Integer.valueOf(lineL[2])>0)
						{
							arrayLMapCandidate.put(textL, (double)Integer.valueOf(lineL[2]));
						}				
					}
				}

				for(int i=0; i < sentencesL.length; i++){
					//if(sentencesL[i].length()!=0){
					String lineL[] = sentencesL[i].split("\t");
					Document doc2 = new Document();
					String textL = lineL[1];
					//String npSenence = computeNP(textL);

					arrayLMap.put(textL, Integer.valueOf(lineL[2]));

					doc2.add(new Field("content",textL, Store.YES, Index.ANALYZED));
					//System.out.println(sentences.get(i));

					indexWriter.addDocument(doc2);
					//}
				}
           // continue;
			case 1:
				citeText = lematizer.lemmatizeText(fileS.split("\t")[1].replaceAll("-"," "));
				npQurey = citeText;
				citationText = npQurey;
				for(int i=0; i < sentencesL.length; i++){
					if(sentencesL[i].length()!=0){
						String lineL[] = sentencesL[i].split("\t");
						//Document doc2 = new Document();
						String textL = lineL[1];
						//String npSenence = computeNP(textL);

						//List of sentences
						//arrayLMap.put(lematizer.lemmatizeStemText(npSenence), Integer.valueOf(lineL[2]));
						if(Integer.valueOf(lineL[2])>0)
						{
							arrayLMapCandidate.put(lematizer.lemmatizeText(textL), (double)Integer.valueOf(lineL[2]));
						}				
					}
				}

				for(int i=0; i < sentencesL.length; i++){
					//if(sentencesL[i].length()!=0){
					String lineL[] = sentencesL[i].split("\t");
					Document doc2 = new Document();
					String textL = lineL[1];
					//String npSenence = computeNP(textL);

					arrayLMap.put(lematizer.lemmatizeText(textL), Integer.valueOf(lineL[2]));

					doc2.add(new Field("content",lematizer.lemmatizeText(textL), Store.YES, Index.ANALYZED));
					//System.out.println(sentences.get(i));

					indexWriter.addDocument(doc2);
					//}

				}
			case 2:
				citeText = lematizer.lemmatizeStemText(fileS.split("\t")[1].replaceAll("-"," "));
				npQurey = citeText;
				citationText = npQurey;
				for(int i=0; i < sentencesL.length; i++){
					if(sentencesL[i].length()!=0){
						String lineL[] = sentencesL[i].split("\t");
						//Document doc2 = new Document();

						String textL = lineL[1];
						//String npSenence = computeNP(textL);

						//List of sentences
						//arrayLMap.put(lematizer.lemmatizeStemText(npSenence), Integer.valueOf(lineL[2]));
						if(Integer.valueOf(lineL[2])>0)
						{
							arrayLMapCandidate.put(lematizer.lemmatizeStemText(textL), (double)Integer.valueOf(lineL[2]));
						}				
					}
				}

				for(int i=0; i < sentencesL.length; i++){
					//if(sentencesL[i].length()!=0){
					String lineL[] = sentencesL[i].split("\t");
					Document doc2 = new Document();
					String textL = lineL[1];
					//String npSenence = computeNP(textL);

					arrayLMap.put(lematizer.lemmatizeStemText(textL), Integer.valueOf(lineL[2]));

					doc2.add(new Field("content",lematizer.lemmatizeStemText(textL), Store.YES, Index.ANALYZED));
					//System.out.println(sentences.get(i));

					indexWriter.addDocument(doc2);
					//}

				}

		}// switch
			indexWriter.commit();
			int count = 0;
			IndexReader indexReader = DirectoryReader.open(ramdir);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			indexSearcher.setSimilarity(getSimilarity(algo[k]));
			BooleanQuery booleanQuery = new BooleanQuery();
			String [] wordInListSplit = citationText.toLowerCase().split(" ");
			for (String word: wordInListSplit){

				booleanQuery.add(new TermQuery(new Term("content", word.trim())), Occur.SHOULD );
			}

			if(arrayLMapCandidate.size()>0){
				TopDocs topDocs = indexSearcher.search(booleanQuery, arrayLMapCandidate.size());
				HashMap<String, Double> hashMapFromRanking = new HashMap<String, Double>();

				for (ScoreDoc scoreDoc : topDocs.scoreDocs) {

					int docId = scoreDoc.doc;

					doc = indexSearcher.doc(docId);
					//System.out.println(arrayLMap.get(doc.get("content"))+", "+scoreDoc.score+", "+ doc.get("content"));				
					if(arrayLMap.get(doc.get("content"))>0){ //Check if document is a linkage candidate
						hashMapFromRanking.put(doc.get("content"), (double)arrayLMap.get(doc.get("content")));
						count++;
					}

				}
				WeightedNormalizedDiscountedCumulativeGain  wNDCG = new WeightedNormalizedDiscountedCumulativeGain();

				double wNDCGValue = 
						wNDCG.evaluateInstance(arrayLMapCandidate, hashMapFromRanking, 0);
				wNDCGValAg+=wNDCGValue;
				System.out.println("Precision sentences # :"+ count);
				//output.write("Precision at " +listCandidate.size() +" :"  +(double)count/precisionNumber+"\n"+"\n");
				System.out.println("Precision at " +arrayLMapCandidate.size() +" :"  +(double)count/arrayLMapCandidate.size());
				precision.add((double)count/arrayLMapCandidate.size());
				precisionsnDCG.add(wNDCGValue);
				System.out.println("WNCG: " +wNDCGValue);
				double linkageScore = (double)count/arrayLMapCandidate.size();
				score += linkageScore;


			} // partial evaluation

		} // end search algos
		//System.out.println(precision);
		//System.out.println(precisionsnDCG);
		//System.out.println(calculateAverage(precisionsnDCG));
		System.out.println();
		//}
		
		return precisionsnDCG;
	}
	private static double calculateAverage(ArrayList <Double> marks) {
		Double sum = 0.0;
		if(!marks.isEmpty()) {
			for (Double mark : marks) {
				sum += mark;
			}
			return sum.doubleValue() / marks.size();
		}
		return sum;
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

	private static String computeNP(String textQuery){
		//List<String> qTokens = new ArrayList<String>();
		//String qText16 = "Formalin fixation has many advantages such as the ease of tissue handling, the possibility of long-term storage, an optimal histological quality and its availability in large quantities at low price.";		
		String nounPhrase = "";		
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
				nounPhrase += word+" ";
			}
		}

		return nounPhrase;
	}
	private static Query computeNPQuery(String nounPhrase){
		BooleanQuery query = new BooleanQuery();

		String [] wordInListSplit = nounPhrase.split(" ");
		for (String word: wordInListSplit){

			query.add(new TermQuery(new Term("content", word)), Occur.SHOULD );
		}
		return query;
	}
}
