package dz.aak.sentrep.ston;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ReqCreator {

	private HashMap<String, ReqRolePlayer> players = new HashMap<String, ReqRolePlayer>();
	private HashMap<String, ReqAction> actions = new HashMap<String, ReqAction>();
	private ArrayList<ReqSentence> sentences = new ArrayList<ReqSentence>();
	
	//private ReqDisjunction subjects = new ReqDisjunction();
	//private ReqDisjunction objects = new ReqDisjunction();
	ReqClause currentRelative;
	
	public ReqCreator() {
	}
	
	public ReqCreator(HashMap<String, ReqRolePlayer> players, 
			HashMap<String, ReqAction> actions, ArrayList<ReqSentence> sentences){
		
		this.players = new HashMap<String, ReqRolePlayer>(players);
		this.actions = new HashMap<String, ReqAction>(actions);
		this.sentences = new ArrayList<ReqSentence>(sentences);
	}
	
	
	public boolean addRolePlayer(String id, int nounSynSet){
		id = id.trim();
		ReqRolePlayer player = ReqRolePlayer.create(id, nounSynSet);
		if (player == null){
			//System.out.println("id already exists");
			return false;
		}
		players.put(id.trim(), player);
		return true;
	}
	
	public boolean addAction(String id, int verbSynSet){
		id = id.trim();
		ReqAction action = ReqAction.create(id, verbSynSet);
		if (action == null){
			//System.out.println("id already exists");
			return false;
		}
		actions.put(id, action);
		return true;
	}
	
	public boolean addSentence(ReqSentence sentence){
		sentences.add(sentence);
		return true;
	}
	
	/*private boolean verifyExistance (String actionId, String roleId){
		actionId = actionId.trim();
		roleId = roleId.trim();
		if (! actions.containsKey(actionId)) return false;
		if (players.containsKey(roleId)) return true;

		
		return false;
	}*/
	
	
	
	public boolean addRelative(String type, String id){

		currentRelative = new ReqClause(type);
		
		if (actions.containsKey(id)){
			actions.get(id).addRelative(currentRelative);
		} else {
			if (players.containsKey(id)){
				players.get(id).addRelative(currentRelative);
			} else{
				return false;
			}
		}

		return true;
	}
	
	public boolean addRelativeConjunctions(Set<String> relativeConjunctions){
		currentRelative.addConjunctedPredicates(relativeConjunctions);
		return true;
	}
	
	public boolean addRelativeConjunctions(String... relativeConjunctions){
		HashSet<String> relConjunctions = new HashSet<String>();
		for (String rel: relativeConjunctions)
			relConjunctions.add(rel);
		return addRelativeConjunctions(relConjunctions);
	}
	
	public boolean addSubjectConjunctions(String actionId, Set<String> subjectsIDs){
		if (! actions.containsKey(actionId)) return false;
		
		//For now, we don't verify the existance of each subject in players list
		actions.get(actionId).addSubjects(subjectsIDs);
		return true;
	}
	
	public boolean addObjectConjunctions(String actionId, Set<String> objectsIDs){
		if (! actions.containsKey(actionId)) return false;
		
		//For now, we don't verify the existance of each object in players list
		actions.get(actionId).addObjects(objectsIDs);
		return true;
	}
	
	
	public boolean addVerbSpecif(String actionId, String tense, String modality, boolean progressive, boolean negated){
		actionId = actionId.trim();
		if (! actions.containsKey(actionId)) return false;
		actions.get(actionId).addVerbSpecif(tense, modality, progressive, negated);
		return true;
	}
	
	public boolean addAdjective(String playerId, int adjSynSet, Set<Integer> advSynSets){
		playerId = playerId.trim();
		if (! players.containsKey(playerId)) return false;
		
		ReqRolePlayer player = players.get(playerId);
		player.addAdjective(adjSynSet, advSynSets);
		return true;
	}
	
	public boolean setQuantity(String playerId, String quantity){
		playerId = playerId.trim();
		if (! players.containsKey(playerId)) return false;
		
		players.get(playerId).setQuantity(quantity);
		return true;
	}
	
	
	public String getStructuredRequest(){
		
		String result = "@roles:";
		
		if(players.isEmpty()){
			result += " null;\n";
		} else {
			result += " r:[\n";
			Iterator<ReqRolePlayer> it = players.values().iterator();
			while (it.hasNext()){
				result += it.next().structuredString() + "\n";
			}
				
			result += "r:]\n";
		}
		
		//////////////////////////
		if(! actions.isEmpty()){
			result += "\n@actions: act:[\n";
			
			Iterator<ReqAction> it = actions.values().iterator();
			while (it.hasNext()){
				result += it.next().structuredString() + "\n";
			}
				
			result += "act:]\n";
		}		
		//////////////////////////
		
		return result;
	}
	
	public String getRequest(){
		
		String result = "@roles:";
		
		if(players.isEmpty()){
			result += "null;";
		} else {
			result += "r:[";
			Iterator<ReqRolePlayer> it = players.values().iterator();
			while (it.hasNext()){
				result += it.next();
			}
				
			result += "r:]";
		}
		
		//////////////////////////
		if(! actions.isEmpty()){
			result += "@actions:act:[";
			
			Iterator<ReqAction> it = actions.values().iterator();
			while (it.hasNext()){
				result += it.next();
			}
				
			result += "act:]";
		}		
		//////////////////////////
		
		
		
		return result;
	}
	
	

}
