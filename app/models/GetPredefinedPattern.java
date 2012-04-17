package models;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.management.RuntimeErrorException;

/**
 * Get pertained queries. The queries are loaded form  text files.
 * @author sobermeier
 *
 */
public class GetPredefinedPattern {
	private static GetPredefinedPattern instance;
	private static HashMap<String,String> loadedPattern;

	private GetPredefinedPattern(){
		loadedPattern = new HashMap<String,String> ();
		loadedPattern.put("play-epsparql-m12-jeans-example-query.eprq", getPatternFormFile("play-epsparql-m12-jeans-example-query.eprq"));
	}


	public static String getPattern(String patternName){
		
		if(instance==null){
			instance = new GetPredefinedPattern();
		}
		
		return loadedPattern.get(patternName);
	}
	
	private String getPatternFormFile(String queryFile){
		try {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(queryFile);
			BufferedReader br =new BufferedReader(new InputStreamReader(is));
			StringBuffer sb = new StringBuffer();
			String line;
			
			while (null != (line = br.readLine())) {
					sb.append(line);
			}
			br.close();
			is.close();

			return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("It is not possible to load pattern " + queryFile + "from file" );
		}
	
	}

}
