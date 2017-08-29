package cleanlinkage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class BuildTableArticle {
	
	public static void main(String[] args){
		
		
		String file ="C:/Users/Hospice/Desktop/1471-2091-10-18.txt";
		String[] filearray = ReadLinesFromFile(file);
		String output = "\\begin{table*}[!h]\n\\begin{tabular}{|p{\\tabTwo}|c|c|c|}\n\\hline\nNumber &Sentence&Rating\n"+"\\hline\n";
		int n = 1;
		for(String line:filearray){
			line = line.replace("%", "\\%").replace("°C", "$^\\circ$ C");
			output+=n+++"&"+line.split("\t")[0]+"&"+line.split("\t")[1]+"\\\\"+"\n"+"\\hline"+"\n";
			
		}
		output+="\\end{tabular}\n\\end{table}\\caption{Complete Annotation Paper }\\label{fullannotation}\n\\end{table*}";
		System.out.println(output);
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
}
