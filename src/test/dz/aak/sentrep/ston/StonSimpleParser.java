package dz.aak.sentrep.ston;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

public class StonSimpleParser extends Parser {

	static String testFile = "ston/test.ston";
	
	private boolean firstDisjunction = true;
	
	public StonSimpleParser(String description) {
		this.parse(description);
	}

	@Override
	protected void addAction(String id, int synSet) {
		System.out.println("Adding action: " + id + ", verb: " + synSet);

	}

	@Override
	protected void addVerbSpecif(String tense, String modality,
			boolean progressive, boolean negated) {
		String verbDesc = "\tTense=" + tense;
		verbDesc += (modality.matches("none"))? "" : ", Modality= " + modality;
		verbDesc += (progressive)? ", Progressive": "";
		verbDesc += (negated)? ", Negated": "";
		System.out.println(verbDesc);
	}

	@Override
	protected void actionFail() {
		System.out.println("ACTION FAILED");
	}

	@Override
	protected void addSubjects() {
		System.out.println("\tSubjects: ");
		firstDisjunction = true;
	}

	@Override
	protected void addObjects() {
		System.out.println("\tObjects: ");
		firstDisjunction = true;
	}

	@Override
	protected void addRole(String id, int synSet) {
		System.out.println("Adding role: " + id + ", noun: " + synSet);
	}

	@Override
	protected void addAdjective(int synSet, Set<Integer> advSynSets) {
		System.out.println("\tAdjective: " + synSet + ", adverbs: " + advSynSets);
	}

	@Override
	protected void adjectiveFail() {
		System.out.println("ADJECTIVE FAILED");
	}

	@Override
	protected void roleFail() {
		System.out.println("ROLE FAILED");
	}

	@Override
	protected void adpositionalFail() {
		System.out.println("ADPOSITION FAILED");

	}

	@Override
	protected void addAdpositional(String type) {
		System.out.println("Add adpositional clause type: " + type);
	}

	@Override
	protected void addConjunctions(Set<String> IDs) {
		String res = (firstDisjunction)? "\t\t": "\t\tor ";
		System.out.println(res + IDs);
		firstDisjunction = false;
	}

	@Override
	protected void parseSuccess() {
		System.out.println("Congratulations: SUCCESS");
	}

	@Override
	protected void parseFail() {
		System.out.println("General structure not respected");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String specification = readFile(testFile);
		new StonSimpleParser(specification);
	}
	
	public static String readFile (String f) {
		try {
			String contents = "";

			BufferedReader input = new BufferedReader(new FileReader(f));

			
			for(String line = input.readLine(); line != null; line = input.readLine()) {
				contents += line + "\n";
			}
			input.close();

			return contents;

		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		} 
	}


}
