import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class JudgeMaliciousSentence {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FileReader fr = null;
		BufferedReader br = null;
		try {

			fr = new FileReader(
					"c:/users/dyson/desktop/java_workspace/stanfordParser/maliciousSentences/output_Imperative_004_malicious.txt");

			br = new BufferedReader(fr);

			PrintWriter pw2 = new PrintWriter(new FileWriter(
					"c:/users/dyson/desktop/java_workspace/stanfordParser/maliciousSentencesOnlyOBJ/output_Imperative_004_OBJ.txt", true));

			String value;
			while ((value = br.readLine()) != null) {
				if(value.contains("verb :")) {
					continue;
				}
				else {
					pw2.println(value);
				}
			}
			pw2.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}
