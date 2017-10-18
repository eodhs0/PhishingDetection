import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.process.WordTokenFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

class ParserDemo {

	/**
	 * 
	 * The main method demonstrates the easiest way to load a parser. Simply call
	 * loadModel and specify the path of a serialized grammar model, which can be a
	 * file, a resource on the classpath, or even a URL. For example, this
	 * demonstrates loading a grammar from the models jar file, which you therefore
	 * need to include on the classpath for ParserDemo to work.
	 * 
	 * Usage: {@code java ParserDemo [[model] textFile]} e.g.: java ParserDemo
	 * edu/stanford/nlp/models/lexparser/chineseFactored.ser.gz
	 * data/chinese-onesent-utf8.txt
	 *
	 */

	public static void main(String[] args) throws IOException {
		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		if (args.length > 0) {
			parserModel = args[0];
		}

		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);
		// System.out.println(lp.tagIndex); // Index of each tags
		demoDP(lp, "C:\\Users\\dyson\\Desktop\\java_workspace\\stanfordParser\\parseTest.txt");
	}

	/**
	 * demoDP demonstrates turning a file into tokens and then parse trees. Note
	 * that the trees are printed by calling pennPrint on the Tree object. It is
	 * also possible to pass a PrintWriter to pennPrint if you want to capture the
	 * output. This code will work with any supported language.
	 * 
	 * @throws IOException
	 */
	public static void demoDP(LexicalizedParser lp, String filename) throws IOException {
		// This option shows loading, sentence-segmenting and tokenizing
		// a file using DocumentPreprocessor.

		/* File writer contructor pw2 */
		PrintWriter pw2 = new PrintWriter(
				new FileWriter("c:/users/dyson/desktop/java_workspace/stanfordParser/out.txt", true));
		System.out.println("-----start of demoDP-----");

		TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
		GrammaticalStructureFactory gsf = null;
		if (tlp.supportsGrammaticalStructures()) {
			gsf = tlp.grammaticalStructureFactory();
		}

		// You could also create a tokenizer here (as below) and pass it
		// to DocumentPreprocessor

		for (List<HasWord> sentence : new DocumentPreprocessor(
				"C:\\Users\\dyson\\Desktop\\java_workspace\\stanfordParser\\\\parseTest.txt")) {
			System.out.println("---------------------------------------------------------------");
			String connectElement = "";
			for (int k = 0; k < sentence.size(); k++) {
				System.out.print(sentence.get(k) + " ");
				connectElement += sentence.get(k) + " ";
			}
			pw2.println(connectElement);
			System.out.println();

			// System.out.println(sentence); // List of the tokenized result

			Tree parse = lp.apply(sentence);
			Tree parse2 = lp.apply(sentence);
			parse.pennPrint(); // Print Tree

			if (parse.firstChild().getNodeNumber(1).toString().contains("SQ")) {
				continue;
			} else {
				/*
				 * System.out.println(parse.firstChild().getNodeNumber(1).toString().contains(
				 * "SQ")); System.out.println(parse.firstChild().nodeString().charAt(0));
				 * System.out.println(parse.firstChild().getChildrenAsList());
				 * System.out.println(parse.yield()); System.out.println(parse.labeledYield());
				 * System.out.println("labeledYield " + parse.firstChild().labeledYield());
				 */

				TregexPattern noNP = TregexPattern.compile("((@VP0 > S $ S) $ADVP !$,,@NP)");
				TregexMatcher n = noNP.matcher(parse);
				int modifyParseTree = 0;
				String ExtFirstWord = "";
				String ExtWordNTag = "";

				while (n.find()) {
					System.out.println("******");
					n.getMatch().pennPrint();
					parse2 = n.getMatch();
					modifyParseTree = 1;
				}

				// if the sentence that doesn't need to judge is removed above while iteration
				if (modifyParseTree == 1) {
					ExtFirstWord = parse2.firstChild().getLeaves().get(0).toString().toLowerCase();
				} else {
					ExtFirstWord = parse.firstChild().getLeaves().get(0).toString().toLowerCase(); // Extract the first
																									// word
					ExtWordNTag = parse.taggedYield().get(0).toString().toLowerCase(); // Extract each word with tags
				}

				int modalFlag = 0;

				ArrayList<TaggedWord> listedTaggedString = parse.taggedYield();

				// Judge the suggestion sentence
				for (int i = 0; i < listedTaggedString.size() - 1; i++) {
					if (listedTaggedString.get(i).toString().toLowerCase().contentEquals("should/md")
							|| listedTaggedString.get(i).toString().toLowerCase().contentEquals("would/md")
							|| listedTaggedString.get(i).toString().toLowerCase().contentEquals("'d/md")
							|| listedTaggedString.get(i).toString().toLowerCase().contentEquals("could/md")
							|| listedTaggedString.get(i).toString().toLowerCase().contentEquals("might/md")
							|| listedTaggedString.get(i).toString().toLowerCase().contentEquals("may/md")
							|| listedTaggedString.get(i).toString().toLowerCase().contentEquals("must/md")
							|| (listedTaggedString.get(i).toString().toLowerCase().contentEquals("have/vbp")
									&& listedTaggedString.get(i + 1).toString().toLowerCase().contentEquals("to/to"))) {
						if (i != 0 && listedTaggedString.get(i - 1).toString().toLowerCase().contentEquals("you/prp")) {
							modalFlag = 1;
							break;
						}
					}
				}

				if (ExtFirstWord.contentEquals("give") || ExtFirstWord.contentEquals("send")
						|| ExtFirstWord.contentEquals("donate") || ExtFirstWord.contentEquals("ensure")) // 동사 셋으로
																											// 판단해야함.
					System.out.println("It is Imperatives command.");
				else if (ExtFirstWord.contentEquals("please")) // 동사 셋으로 판단해야함.
					System.out.println("It is Polite prefixes.");
				else if (modalFlag == 1)
					System.out.println("It is Suggestion.");

				System.out.println(parse.taggedYield()); // 각 word / tag 를 리스트로 출력
				// System.out.println(parse.toString().replaceAll("[()]", "")); // 정규식써서 Tree 출력
				// 결과에서 괄호 다 없애기
				// System.out.println("First Word 1 : " + ExtFirstWord);
				// System.out.println("Tag 1 : " + ExtWordNTag);

				if (gsf != null) {
					GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
					// Collection tdl = gs.typedDependenciesCCprocessed();
					List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();

					/*
					 * System.out.println("get gov : " + tdl.get(0).gov()); // 1번째 단어와 단어의 태그 출력 ex)
					 * send/VB System.out.println("get gov : " + tdl.get(0).gov().value()); // 1번째
					 * 단어만 출력 System.out.println("get gov : " + tdl.get(0).gov().tag()); // 1번째 단어의
					 * 태그 출력 System.out.println("get dep : " + tdl.get(0).dep()); // ? I don't know
					 * yet.. System.out.println("get reln : " + tdl.get(0).reln()); // get(0)의 문법적
					 * 의미 ex) advmod, nsubj
					 */
					
					// Judge the desire sentence
					for (int i = 0; i < tdl.size(); i++) {
						String extractElement = tdl.get(i).reln().toString();
						if (extractElement.equals("nsubj")) {
							if (tdl.get(i).gov().value().toString().toLowerCase().equals("want")
									|| tdl.get(i).gov().value().toString().toLowerCase().equals("hope")
									|| tdl.get(i).gov().value().toString().toLowerCase().equals("wish")
									|| tdl.get(i).gov().value().toString().toLowerCase().equals("desire")) {
								System.out.println("It is desire sentence.");
							}
						}
						// It is extract function as following paper.
						/*
						 * if(extractElement.equals("dobj")) {
						 * if(tdl.get(i).gov().value().toString().toLowerCase().equals("want") ||
						 * tdl.get(i).gov().value().toString().toLowerCase().equals("hope") ||
						 * tdl.get(i).gov().value().toString().toLowerCase().equals("wish") ||
						 * tdl.get(i).gov().value().toString().toLowerCase().equals("desire")) {
						 * if(tdl.get(i).dep().value().toString().toLowerCase().equals("you")) {
						 * System.out.println("It is desire sentence."); } } }
						 * if(extractElement.equals("nsubjpass")) {
						 * if(tdl.get(i).gov().value().toString().toLowerCase().equals("want") ||
						 * tdl.get(i).gov().value().toString().toLowerCase().equals("hope") ||
						 * tdl.get(i).gov().value().toString().toLowerCase().equals("wish") ||
						 * tdl.get(i).gov().value().toString().toLowerCase().equals("desire")) {
						 * if(tdl.get(i).dep().value().toString().toLowerCase().equals("you")) {
						 * System.out.println("It is desire sentence."); } } }
						 */
					}

					// extract the each element that is "nsubj", "nsubjpass", "dobj", "aux".
					System.out.println("*****-----Grammartic Extract-----*****");
					for (int i = 0; i < tdl.size(); i++) { // System.out.println(tdl.get(i));
						String extractElement = tdl.get(i).reln().toString();
						if (extractElement.equals("nsubj") || extractElement.equals("nsubjpass")
								|| extractElement.equals("dobj") || extractElement.equals("aux")
								|| extractElement.equals("auxpass")) {
							// System.out.println(tdl.get(i));
							//pw2.println(tdl.get(i));
						}
						System.out.println(tdl.get(i));
					}
				}

			}
			System.out.println();
		}
		pw2.close();
	}

	private ParserDemo() {
	} // static methods only
}