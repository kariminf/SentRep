package dz.aak.sentrep.ston;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Abdelkrime Aries
 *
 */
public abstract class Parser {

	// These are the characters to be ignored 
	private static String BL = "[\\t \\n\\r]+";
	
	// Adpositional clause: time or location
	private static final String ADPblock = "@adp\\:\\[(.*)adp\\:\\];?";
	
	// Adjective
	private static final String ADJblock = "@adj\\:\\[(.*)adj\\:\\];?";
	
	// Relative clauses
	private static final String RELblock = "@rel\\:\\[(.*)rel\\:\\];?";
	
	// This is the regular expression used to separate main blocks
	private static final Pattern CONT = 
			Pattern.compile("@r\\:\\[(.+)r\\:\\]@act\\:\\[(.+)act\\:\\]@sent\\:\\[(.+)sent\\:\\]");
	
	//This is true when the parsing is a success
	private boolean success = false;
	
	
	/**
	 * The constructor of the parser
	 */
	public Parser() {
		success = false;
	}
	
	/**
	 * The method to parse a STON description
	 * @param description STON description
	 */
	public void parse(String description){
		
		description = description.replaceAll(BL, "");
		description = description.toLowerCase();
		
		//System.out.println(description);
		Matcher m = CONT.matcher(description);
		if (m.find()) {
			String roles =  m.group(1);
			String actions =  m.group(2);
			if (! parseRoles(roles)) return;
			if (! parseActions(actions)) return;
        }
		
		success = true;
		
		parseSuccess();
	}
	
	/**
	 * This function tells us if the parsing goes well or not
	 * @return True: if the parsing succeed, False: else.
	 */
	public boolean parsed(){
		return success;
	}
	
	
	/**
	 * Parses the roles' block
	 * @param description STON description of roles
	 * @return True if there is no error
	 */
	private boolean parseRoles(String description){
		
		int idx;
		while ((idx = description.indexOf("r:}")) >= 0) {
			String role =  description.substring(3, idx);
			description = description.substring(idx+3);
			//System.out.println(role);
			if (! parseRole(role))
				return false;
        }

		return true;

	}
	
	/**
	 * Parses the actions' block
	 * @param description STON description of actions
	 * @return True if there is no error
	 */
	private boolean parseActions (String description){
		
		int idx;

		while ((idx = description.indexOf("act:}")) >= 0) {
			String action =  description.substring(5, idx);
			description = description.substring(idx+5);
			//System.out.println(role);
			if (! parseAction(action))
				return false;
			
        }
		
		return true;
	}
	
	
	/**
	 * Parses one action block
	 * @param description STON description of one action
	 * @return True if there is no error
	 */
	private boolean parseAction(String description){
		
		String id = "";
		String synSetStr = "";
		String tense = "";
		boolean progressive = false;
		boolean negated = false;
		String modality = "none";
		String subjects = "";
		String objects = "";
		
		String adpositional = "";
		
		if (description.contains("@adp")){
			
			Pattern timesPattern = 
				Pattern.compile("(.*)" + ADPblock + "(.*)");
			Matcher m = timesPattern.matcher(description);
			if (m.find()){
				adpositional = m.group(2);
				//System.out.println("time found");
				description = m.group(1) + m.group(3);		
			}
		}
		
		for (String desc : description.split(";")){
			
			desc = desc.trim();
			
			if(desc.startsWith("id:")){
				id = desc.split(":")[1];
				continue;
			}
			
			if(desc.startsWith("synset:")){
				synSetStr = desc.split(":")[1];
				continue;
			}
			
			if(desc.startsWith("tense:")){
				tense = desc.split(":")[1];
				continue;
			}
			
			if(desc.startsWith("progressive:")){
				String aspect = desc.split(":")[1];
				aspect = aspect.trim().toUpperCase();
				
				if(aspect.matches("yes")){
					progressive = true;
				}
				continue;
			}
			
			if(desc.startsWith("negated:")){
				String negate = desc.split(":")[1];
				negate = negate.trim().toUpperCase();

				if(negate.matches("yes")){
					negated = true;
				}
				continue;
			}
			
			if(desc.startsWith("modality:")){
				modality = desc.split(":")[1];
				continue;
			}
			
			if(desc.startsWith("subjects:")){
				subjects = desc.split(":")[1];
				continue;
			}
			
			if(desc.startsWith("objects:")){
				objects = desc.split(":")[1];
				continue;
			}
		}
		
		//The action must have an ID
		if(id.length() < 1){
			actionFail();
			success = false;
			return false;
		}
		
		// An action must have a synset which is a number
		if (! synSetStr.matches("\\d+")){
			roleFail();
			success = false;
			return false;
		}
		
		int synSet = Integer.parseInt(synSetStr);
		
		addAction(id, synSet);
		
		// There are three tenses
		if(! tense.matches("past|present|future")){
			tense = "present";
		}
		
		// There are three modalities: permissibility, possibility and obligation
		if(! modality.matches("can|may|must")){
			modality = "none";
		}
		
		//Defines verb specifications
		addVerbSpecif(tense, modality, progressive, negated);

		// Process subjects
		if(subjects.length() > 2){
			if (!(subjects.startsWith("[") && subjects.endsWith("]"))){
				//System.out.println("subjects=" + subjects);
				return false;
			}
			
			subjects = subjects.substring(1, subjects.length()-1);
			addSubjects();
			parseComponents(subjects);

		}
		
		//Process objects
		if(objects.length() > 2){
			if (!(objects.startsWith("[") && objects.endsWith("]")))
				return false;
			objects = objects.substring(1, objects.length()-1);
			addObjects();
			parseComponents(objects);

		}
		
		// Process time and place
		if (adpositional.length()>0){
			if (! parseAdpositionals(adpositional)) return false;
		}
		
		return true;
	}

	/**
	 * Subjects and objects are disjunctions of conjunctions, they are 
	 * represented like [id11, ...|id21, ... | ...] <br/>
	 * For example: [mother, son|father] means: mother and son or father
	 * @param description STON description of IDs
	 * @return True if there is no error
	 */
	private boolean parseComponents(String description){
		String[] disjunctions = description.split("\\|");
		
		for (String disjunction: disjunctions){
			Set<String> conjunctions = new HashSet<String>();
			for (String conjunction: disjunction.split(",")){
				conjunctions.add(conjunction);
				
			}
			addConjunctions(conjunctions);
			
		}
		return true;
	}
	

	/**
	 * Parse the 
	 * @param description
	 * @return
	 */
	private boolean parseAdjectives(String description){
		
		int idx;
		while ((idx = description.indexOf("adj:}")) >= 0) {
			String adjective =  description.substring(5, idx);
			description = description.substring(idx+5);
			if (description.startsWith(","))
				description = description.substring(1);
			
			String[] descs = adjective.split(";");
			
			int synSet = 0;
			HashSet<Integer> advSynSets = new HashSet<Integer>();
			for (String desc: descs){
				if(desc.startsWith("synset:")){
					String synSetStr = desc.split(":")[1];
					synSet = Integer.parseInt(synSetStr);
					continue;
				}
				
				if(desc.startsWith("adverbs:")){
					String synSetStrs = desc.split(":")[1];
					synSetStrs = synSetStrs.substring(1, synSetStrs.length()-1);
					
					for (String AdvsynSetStr: synSetStrs.split(",")){
						
						int AdvsynSet = Integer.parseInt(AdvsynSetStr);
						advSynSets.add(AdvsynSet);
					}
				}
				
			}
			
			if (synSet < 1){
				adjectiveFail();
				success = false;
				return false;
			}
			
			addAdjective(synSet, advSynSets);
			
        }
		
		return true;
	}
	

	//TODO complete role relatives
	private boolean parseRRelatives(String description){
		
		return true;
	}

	private boolean parseRole(String description){
		
		String id = "";
		String synSetStr = "";
		String adjectives = "";
		String relatives = "";
		
		if (description.contains("@adj")){
			
			Pattern adpPattern = 
				Pattern.compile("(.*)" + ADJblock + "(.*)");
			Matcher m = adpPattern.matcher(description);
			if (m.find()){
				adjectives = m.group(2);
				//System.out.println("time found");
				description = m.group(1) + m.group(3);		
			}
		}
		
		if (description.contains("@rel")){
			
			Pattern adpPattern = 
				Pattern.compile("(.*)" + RELblock + "(.*)");
			Matcher m = adpPattern.matcher(description);
			if (m.find()){
				relatives = m.group(2);
				//System.out.println("time found");
				description = m.group(1) + m.group(3);		
			}
		}

		for (String desc: description.split(";")){
			
			desc = desc.trim();
			
			if(desc.startsWith("id:")){
				id = desc.split(":")[1];
				continue;
			}
			
			if(desc.startsWith("synset:")){
				synSetStr = desc.split(":")[1];
				continue;
			}
		}
		
		//A role must have an ID
		if (id.length() < 1){
			roleFail();
			success = false;
			return false;
		}
		
		// A role must have a synset which is a number
		if (! synSetStr.matches("\\d+")){
			roleFail();
			success = false;
			return false;
		}
		
		int synSet = Integer.parseInt(synSetStr);
		
		// Add the role 
		addRole(id, synSet);
		
		//Process adjectives
		if (adjectives.length() > 0){
			if (! parseAdjectives(adjectives)) return false;
		}
		
		//Process relatives
		if (relatives.length() > 0){
			if (! parseRRelatives(relatives)) return false;
		}
		
		return true;
		
	}

	/**
	 * 
	 * @param description
	 * @return
	 */
	private boolean parseAdpositionals(String description){
		//TODO fix the adpositionals content
		int idx;
		while ((idx = description.indexOf("adp:}")) >= 0) {
			String times =  description.substring(5, idx);
			description = description.substring(idx+5);
			
			/*
			Matcher m = Pattern.compile("synset\\:([^;]+)(;|$)").matcher(times);
			if (! m.find()){
				success = false;
				return false;
			}
			
			String synSetStr = m.group(1);
			
			int synSet = Integer.parseInt(synSetStr);
			
			addAddpositional(synSet);
			
			m = Pattern.compile("predicates\\:\\[(.+)\\]").matcher(times);
			if ( m.find()){
				String predicates = m.group(1);
				parseComponents(predicates);
			}
			*/
			
        }
		
		return true;
	}
	

	
	//Action
	/**
	 * It is called when the parser finds an action
	 * @param id each action has a unique ID
	 * @param synSet this is the synset
	 */
	protected abstract void addAction(String id, int synSet);
	
	/**
	 * It is called to define the specifications of a verb
	 * @param tense It is the tense of the verb: past, present or future
	 * @param modality It is the modal verb: can, must, may, none
	 * @param progressive the action is progressive or not
	 * @param negated the action is negated or not
	 */
	protected abstract void addVerbSpecif(String tense, String modality, boolean progressive, boolean negated);
	
	/**
	 * It is called when the action is failed; It means when the parser find something
	 * wrong in the action block
	 */
	protected abstract void actionFail();
	
	//Subjects and Objects in the Action
	/**
	 * It is called when the parser finds subjects
	 */
	protected abstract void addSubjects();
	
	/**
	 * It is called when the parser finds objects
	 */
	protected abstract void addObjects();
	
	//Role
	/**
	 * It is called when the parser finds a role player
	 * @param id each role has a unique ID
	 * @param synSet wordnet synset of the Noun 
	 */
	protected abstract void addRole(String id, int synSet);
	
	/**
	 * It is called when the role player has an adjective
	 * @param synSet wordnet synset of the adjective which modify the noun in the role
	 * @param advSynSets wordnet synsets (Set) of adverbs which modify the adjective
	 */
	protected abstract void addAdjective(int synSet, Set<Integer> advSynSets);
	
	/**
	 * It is called when the parsing of an adjective failed
	 */
	protected abstract void adjectiveFail();
	
	/**
	 * it is called when the parsing of a role failed
	 */
	protected abstract void roleFail();
	
	/**
	 * It is called when the parsing of adpositionals failed
	 */
	protected abstract void adpositionalFail();
	
	/**
	 * It is called to add an adpositional: time or location
	 * @param type the type of the addpositional
	 */
	protected abstract void addAdpositional(String type);
	
	
	//can be used for subjects, objects, places or times
	protected abstract void addConjunctions(Set<String> IDs);
	
	//Parse
	protected abstract void parseSuccess();

}
