package org.edng.lucene4.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.aliasi.util.Files;

public class LinkageSearchWithLuceneEvaluation {

	public LinkageSearchWithLuceneEvaluation() throws IOException{
		//BufferedWriter bw = new BufferedWriter(new FileWriter("C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/IR_30_11/"+"evalTotalCitationndcg_AllAnalysis7_12_20_.txt"));
	}
	public static void main(String args[]) throws Exception{
		computeEvaluationValues();
		
	}

	public static ArrayList<ArrayList<ArrayList<?>>> computeEvaluation() throws Exception{

		BufferedWriter bw = new BufferedWriter(new FileWriter("C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/IR_06_12/"+"evalTotalCitationndcg_AllAnalysis_22at10.txt"));
        
		BufferedWriter outText = new BufferedWriter(new FileWriter("C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/IR_06_12/"+"outRank_AllAnalysis_22at10.txt"));

		
		//String filePath = "C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/newfilesforrteMultiTruthOutAllCandidateMethod/";
		String filePath = "C:/Users/Hospice/Documents/boruta_data/linkageText/newfilesforrteMultiTruth/";
        
        //ArrayList outList = new ArrayList();
		//String fileName = "C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/annotationdataBSAnalysis.txt";
		String fileName = "C:/Users/Hospice/Documents/boruta_data/linkageText/linkageCandidatewithlinkage/citationlistModified22.txt";

		//LinkageSearchWithLuceneAnalysisEvaluationPerPaper linkageSearch = new LinkageSearchWithLuceneAnalysisEvaluationPerPaper();
		String textFromFile = Files.readFromFile(new File(fileName),"ISO-8859-1");
		String[] listFromFile = textFromFile.split("\n");
		int numPaper = listFromFile.length;

		//String dirF = "C:/Users/Hospice/Documents/boruta_data/linkageText/dataperfileIRPart/";
		//for(String file:filesPath){	
		//BufferedWriter bw = new BufferedWriter(new FileWriter(dirF+file));
		//System.out.println(file);
		//for(Algo sim:algo){
		//IBSimilarity	similarity = new IBSimilarity(new DistributionLL(), new LambdaDF(), new NormalizationH1()); 

		
		ArrayList<ArrayList<ArrayList<?>>> outListAll = new ArrayList<ArrayList<ArrayList<?>>>();
		for(String fileS:listFromFile){
			LinkageSearchWithLuceneAnalysisEvaluationPerPaper linkageSearch = new LinkageSearchWithLuceneAnalysisEvaluationPerPaper();

			outText.write("====================== "+fileS+"================="+"\n");
			ArrayList<ArrayList<?>> outList = new ArrayList<ArrayList<?>>();
			outList.add(linkageSearch.computeEvalBaseline(fileS, filePath));
			outList.add(linkageSearch.computeEvalWithStem(fileS, filePath));
			
			outList.add(linkageSearch.computeEvalWithoutStem(fileS, filePath));
			//outList.add(linkageSearch.computeEvaluation(fileS, filePath, 0));

			//outList.add(linkageSearch.computeEvaluation(fileS, filePath, 1));
			//outList.add(linkageSearch.computeEvaluation(fileS, filePath, 2));

			//System.out.println(outList);
			
			outListAll.add(outList);
			bw.write(outList +"\n");
			outText.write(linkageSearch.outputRankedText+"\n\n");

		}
		//System.out.println(outList);
		outText.close();
		bw.close();
		return outListAll;
	}

	public static void computeEvaluationValues() throws Exception{
		ArrayList<ArrayList<ArrayList<?>>> evalValues = new ArrayList<ArrayList<ArrayList<?>>>();
		evalValues =  computeEvaluation();
		LinkageSearchWithLuceneAnalysisEvaluationPerPaper linkageSearch = new LinkageSearchWithLuceneAnalysisEvaluationPerPaper();

		for(int k = 0; k < 3; k++ ){
			for (int i =0; i< linkageSearch.algo.length; i++){
				double valuePerAlgo = 0.0;
				ArrayList arr = new ArrayList();

				for (ArrayList<ArrayList<?>> value: evalValues ){
					//for(int i=0; i<=23; i++){
					ArrayList value0 = value.get(k);
					//for (int t =0; t < value.get(0).size(); t++){
					valuePerAlgo = (Double) value0.get(i);
					arr.add(valuePerAlgo);
					//System.out.println(valuePerAlgo);	
					//}

					//}
				}
				//System.out.println("arr size "+arr.size());
				System.out.println(i + " :Avverage: "+calculateAverage(arr));
			}

			System.out.println("==============================================");
		}
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
}
