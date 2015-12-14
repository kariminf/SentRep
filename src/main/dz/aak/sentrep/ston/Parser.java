package dz.aak.sentrep.ston;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Parser {

	private static String BL = "[\\t \\n\\r]+";
	private static final String TIMESblock = "@times\\:t\\:\\[(.*)t\\:\\];?";
	private static final String PLACESblock = "@places\\:p\\:\\[(.*)p\\:\\];?";
	private static final Pattern CONT = 
			Pattern.compile("@roles\\:r\\:\\[(.+)r\\:\\]@actions\\:act\\:\\[(.+)act\\:\\]");
	
	private boolean success = false;
	/**
	 * 
	 * @param description
	 */
	public Parser() {
		success = false;
	}
	
	public void parse(String description){
		
		description = description.replaceAll(BL, "");
		
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
	
	public boolean parsed(){
		return success;
	}
	
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
	
	
	//TODO complete the action
	private boolean parseAction(String description){
		
		String description2 = description;
		
		String id = "";
		String synSetStr = "";
		String tense = "";
		boolean progressive = false;
		boolean negated = false;
		String modality = "NONE";
		String subjects = "";
		String objects = "";
		
		String times = "";
		String places = "";
		
		if (description2.contains("@times")){
			
			Pattern timesPattern = 
				Pattern.compile("(.*)" + TIMESblock + "(.*)");
			Matcher m = timesPattern.matcher(description2);
			if (m.find()){
				times = m.group(2);
				//System.out.println("time found");
				description2 = m.group(1) + m.group(3);		
			}
		}
		
		if (description2.contains("@places")){
			
			Pattern timesPattern = 
				Pattern.compile("(.*)" + PLACESblock + "(.*)");
			Matcher m = timesPattern.matcher(description2);
			if (m.find()){
				places = m.group(2);
				//System.out.println("time found");
				description2 = m.group(1) + m.group(3);		
			}
		}

		String[] descs = description2.split(";");
		
		for (String desc : descs){
			
			desc = desc.trim();
			
			if(desc.startsWith("id:")){
				id = desc.split(":")[1];
				continue;
			}
			
			if(desc.startsWith("synSet:")){
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
				
				if(aspect.matches("YES")){
					progressive = true;
				}
				continue;
			}
			
			if(desc.startsWith("negated:")){
				String negate = desc.split(":")[1];
				negate = negate.trim().toUpperCase();

				if(negate.matches("YES")){
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
		
		if(id.length() < 1){
			actionFail();
			success = false;
			return false;
		}
		
		int synSet = Integer.parseInt(synSetStr);
		
		addAction(id, synSet);
		
		//TODO add other components of the action
		if(! tense.matches("PAST|PRESENT|FUTURE")){
			tense = "PRESENT";
		}
		
		modality = modality.trim().toUpperCase();
		
		if(! modality.matches("CAN|MAY|MUST")){
			modality = "NONE";
		}
		
		
		addVerbSpecif(tense, modality, progressive, negated);
		
		if (times.length()>0){
			if (! parseTimes(times)) return false;
		}
		
		if (places.length()>0){
			if (! parsePlaces(places)) return false;
		}
		
		
		if(subjects.length() > 2){
			if (!(subjects.startsWith("[") && subjects.endsWith("]"))){
				System.out.println("subjects=" + subjects);
				return false;
			}
				
			subjects = subjects.substring(1, subjects.length()-1);
			addSubjects();
			parseComponents(subjects);

		}
		
		if(objects.length() > 2){
			if (!(objects.startsWith("[") && objects.endsWith("]")))
				return false;
			objects = objects.substring(1, objects.length()-1);
			addObjects();
			parseComponents(objects);

		}
		
		
		return true;
	}

	/**
	 * Subjects and objects are disjunctions of conjunctions, they are 
	 * represented like [id11, ...|id21, ... | ...] <br/>
	 * For example: [mother, son|father] means: mother and son or father
	 * @param description
	 * @return
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
				if(desc.startsWith("synSet:")){
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
	


	private boolean parseRole(String description){
		
		Matcher m = Pattern.compile("id\\:([^;]+)(;|$)").matcher(description);
		
		if (! m.find()){
			roleFail();
			success = false;
			return false;
		}
		
		String id = m.group(1);
		
		m = Pattern.compile("synSet\\:([^;]+)(;|$)").matcher(description);
		
		if (! m.find()){
			roleFail();
			success = false;
			return false;
		}
		
		String synSetStr = m.group(1);
		
		int synSet = Integer.parseInt(synSetStr);
		
		addRole(id, synSet);
		
		m = Pattern.compile("adjectives\\:\\[(.+adj\\:\\})\\]").matcher(description);
		
		if (m.find()){
			String adjectives = m.group(1);
			if (! parseAdjectives(adjectives)) return false;
		}
		
		return true;
		
	}

	/**
	 * 
	 * @param description
	 * @return
	 */
	private boolean parseTimes(String description){
		
		int idx;
		while ((idx = description.indexOf("t:}")) >= 0) {
			String times =  description.substring(3, idx);
			description = description.substring(idx+3);
			
			Matcher m = Pattern.compile("synSet\\:([^;]+)(;|$)").matcher(times);
			if (! m.find()){
				success = false;
				return false;
			}
			
			String synSetStr = m.group(1);
			
			int synSet = Integer.parseInt(synSetStr);
			
			addTime(synSet);
			
			m = Pattern.compile("predicates\\:\\[(.+)\\]").matcher(times);
			if ( m.find()){
				String predicates = m.group(1);
				parseComponents(predicates);
			}
			
        }
		
		return true;
	}
	
	/**
	 * 
	 * @param description
	 * @return
	 */
	private boolean parsePlaces(String description){
		
		int idx;
		while ((idx = description.indexOf("p:}")) >= 0) {
			String times =  description.substring(3, idx);
			description = description.substring(idx+3);
			
			Matcher m = Pattern.compile("synSet\\:([^;]+)(;|$)").matcher(times);
			if (! m.find()){
				success = false;
				return false;
			}
			
			String synSetStr = m.group(1);
			
			int synSet = Integer.parseInt(synSetStr);
			
			addPlace(synSet);
			
			m = Pattern.compile("predicates\\:\\[(.+)\\]").matcher(times);
			if ( m.find()){
				String predicates = m.group(1);
				parseComponents(predicates);
			}
			
        }
		
		return true;
	}
	


	
	//Action
	protected abstract void addAction(String id, int synSet);
	protected abstract void addVerbSpecif(String tense, String modality, boolean progressive, boolean negated);
	protected abstract void actionFail();
	
	//Subjects and Objects in the Action
	protected abstract void addSubjects();
	protected abstract void addObjects();
	
	//Role
	protected abstract void addRole(String id, int synSet);
	protected abstract void addAdjective(int synSet, Set<Integer> advSynSets);
	protected abstract void adjectiveFail();
	protected abstract void roleFail();
	protected abstract void timesFail();
	
	
	protected abstract void addTime(int synSet);
	protected abstract void addPlace(int synSet);

	//can be used for subjects, objects, places or times
	protected abstract void addConjunctions(Set<String> IDs);
	
	//Parse
	protected abstract void parseSuccess();

}
