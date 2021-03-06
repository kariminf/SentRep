package kariminf.sentrep.ston;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import kariminf.sentrep.ston.Parser;

public class StonSimpleParser extends Parser {

	static String testFile = "ston/LouisdeBroglie_Bio/LouisdeBroglie_07.ston";
	
	private boolean firstDisjunction = true;
	
	public StonSimpleParser(String description) {
		this.parse(description);
	}

	@Override
	protected void beginAction(String id, int synSet) {
		System.out.println("Adding action: " + id + ", verb: " + synSet);
	}

	@Override
	protected void addVerbSpecif(String tense, String modality,
			boolean progressive, boolean perfect, boolean negated) {
		String verbDesc = "\tTense=" + tense;
		verbDesc += (modality.matches("none"))? "" : ", Modality= " + modality;
		verbDesc += (progressive)? ", Progressive": "";
		verbDesc += (negated)? ", Negated": "";
		System.out.println(verbDesc);
	}

	@Override
	protected boolean actionFailure() {
		System.out.println("ACTION FAILED");
		return false; //continue parsing
	}

	@Override
	protected void beginAgents() {
		System.out.println("\tAgents: ");
		firstDisjunction = true;
	}

	@Override
	protected void beginThemes() {
		System.out.println("\tThemes: ");
		firstDisjunction = true;
	}

	@Override
	protected void beginRole(String id, int synSet) {
		System.out.println("Adding role: " + id + ", noun: " + synSet);
	}

	@Override
	protected void addAdjective(int synSet, List<Integer> advSynSets) {
		System.out.println("\tAdjective: " + synSet + ", adverbs: " + advSynSets);
	}

	@Override
	protected boolean adjectiveFailure() {
		System.out.println("ADJECTIVE FAILED");
		return false; //continue parsing
	}

	@Override
	protected boolean roleFailure() {
		System.out.println("ROLE FAILED");
		return false; //continue parsing
	}

	@Override
	protected void addConjunctions(List<String> IDs) {
		String res = (firstDisjunction)? "\t\t": "\t\tor ";
		System.out.println(res + IDs);
		firstDisjunction = false;
	}

	@Override
	protected void parseSuccess() {
		System.out.println("Congratulations: SUCCESS");
	}

	@Override
	protected void parseFailure() {
		System.out.println("General structure not respected");
	}
	
	@Override
	protected boolean relativeFailure() {
		System.out.println("RELATIVE FAILED");
		return false; //continue parsing
		
	}

	@Override
	protected void beginRelative(String type) {
		System.out.println("Add relative clause type: " + type);
		
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

	@Override
	protected void endAction(String id, int synSet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void endAgents() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void endThemes() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void beginSentence(String type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void endSentence(String type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void beginActions(boolean mainClause) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void endActions(boolean mainClause) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void beginComparison(String type, List<Integer> adjSynSets) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void addActionAdverb(int advSynSet, List<Integer> advSynSets) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean adverbFailure() {
		return false; //continue parsing
		
	}

	@Override
	protected void addRoleSpecif(String name, String def, String quantity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void beginRole(String id, int synSet, String pronoun) {
		
		
	}

	@Override
	protected void beginPRelatives() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void endPRelatives() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void endRole(String id, int synSet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void endRelative(String SP) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void endComparison(String type, List<Integer> adjSynSets) {
		// TODO Auto-generated method stub
		
	}



}
