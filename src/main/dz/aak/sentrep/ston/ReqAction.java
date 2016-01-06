package dz.aak.sentrep.ston;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReqAction {

	private int verbSynSet;
	private String id;
	private ReqDisjunction subjects = new ReqDisjunction();
	private ReqDisjunction objects = new ReqDisjunction();
	private static Set<String> ids = new HashSet<String>();
	
	private String tense = "PRESENT";
	private String modality = "NONE";
	private boolean progressive = false;
	private boolean negated = false;
	
	/*private HashMap<String, List<ReqClause>> relatives = 
			new HashMap<String, List<ReqClause>>();*/
	
	List<ReqClause> relatives = new ArrayList<ReqClause>();
	
	public void addRelative(ReqClause relative){
		/*String type = relative.getType();
		List<ReqClause> rels = (relatives.containsKey(type))?
				relatives.get(type): new ArrayList<ReqClause>();
		relatives.put(type, rels);
		rels.add(relative);*/
		relatives.add(relative);
	}
	
	/**
	 * @return the verbSynSet
	 */
	public int getVerbSynSet() {
		return verbSynSet;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the subjects
	 */
	public ReqDisjunction getSubjects() {
		return subjects;
	}

	/**
	 * @return the objects
	 */
	public ReqDisjunction getObjects() {
		return objects;
	}

	/**
	 * @return the tense
	 */
	public String getTense() {
		return tense;
	}

	/**
	 * @return the aspect
	 */
	public String getModality() {
		return modality;
	}
	
	private ReqAction(String id, int verbSynSet) {
		this.verbSynSet = verbSynSet;
		this.id = id;
	}
	
	public static ReqAction create(String id, int verbSynSet){
		//protection for same ids
		if(ids.contains(id)){
			System.out.println("id fount:" + id);
			return null;
		}
		ids.add(id);
		return new ReqAction(id, verbSynSet);
	}
	
	public void addVerbSpecif(String tense, String modality, boolean progressive, boolean negated){
		if (tense.matches("PAST|PRESENT|FUTURE")) this.tense = tense;
		if (modality.matches("CAN|MAY|NONE")) this.modality = modality;
		this.progressive = progressive;
		this.negated = negated;
	}
	
	public void addSubjects(Set<String> conjunctions){
		subjects.addConjunctions(conjunctions);
	}
	
	public void addObjects(Set<String> conjunctions){
		objects.addConjunctions(conjunctions);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "act:{";
		
		
		result += "id:" + id ;
		result += ";synSet:" + verbSynSet ;
		result += ";tense:" + tense;
		if (progressive)
			result += ";progressive:YES";
		if (negated)
			result += ";negated:YES";
		if(modality != "NONE")
			result += ";modality:" + modality;
		
		if(! subjects.isEmpty()) {
			result += ";subjects:";
			result += subjects.toString().replace(" ", "");
		}
		
		if(! objects.isEmpty()){
			result += ";objects:";
			result += objects.toString().replace(" ", "");
		}
		
		if (! relatives.isEmpty()){
			result += ";@rel:[";
			for (ReqClause relative: relatives){
				relative.setSpecifs("rel", 0);
				result += relative;
			}
			result += "rel:]";
		}
		
		result += "act:}";
		
		return result;
	}
	
	
	public String structuredString() {
		
		String result = "\tact:{\n";
		
		
		result += "\t\tid: " + id ;
		result += ";\n\t\tsynSet: " + verbSynSet ;
		result += ";\n\t\ttense: " + tense;
		if (progressive)
			result += ";\n\t\tprogressive: YES";
		if (negated)
			result += ";\n\t\tnegated: YES";
		if(modality != "NONE")
			result += ";\n\t\tmodality: " + modality;
		
		if(! subjects.isEmpty()) {
			result += ";\n\t\tsubjects: ";
			result += subjects;
		}
		
		if(! objects.isEmpty()){
			result += ";\n\t\tobjects: ";
			result += objects;
		}
		
		if (! relatives.isEmpty()){
			result += ";\n\t\t@rel:[";
			for (ReqClause relative: relatives){
				relative.setSpecifs("rel", 3);
				result += "\n" + relative.structuredString();
			}
			result += "\n\t\trel:]";
		}
		
		result += "\n\tact:}";
		
		return result;
	}

	

}
