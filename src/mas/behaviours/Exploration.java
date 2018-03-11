package mas.behaviours;

import env.Attribute;
import env.Couple;
import jade.core.behaviours.SimpleBehaviour;
import mas.agents.ExplorationAgent;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Exploration extends SimpleBehaviour {

	private static final long serialVersionUID = 9088209402507795289L;
	private ExplorationAgent explorationAgent;
	// transitionId: 
	// - 0 si boucle sur le meme comportement
	// - 1 si checkVoicemail
	// - 2 si exploration finie
	private int transitionId = 0;

	public Exploration(ExplorationAgent explorationAgent) {
		super(explorationAgent);
		this.explorationAgent = (ExplorationAgent)this.myAgent;
	}

	@Override
	public void action() {
		// Get current position.
		String myPosition = this.explorationAgent.getCurrentPosition();
		// Get agent data
		List<String> openedNodes = this.explorationAgent.getOpenedNodes();
		Map<String, String> exploredNodes = this.explorationAgent.getExploredNodes();
		Map<String, HashSet<String>> map = this.explorationAgent.getMap();

		// On startup, position may not be defined right away.
		if(myPosition.equals("")) return;

		//List of observable from the agent's current position
		List<Couple<String,List<Attribute>>> lobs = this.explorationAgent.observe();
//		System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);/**/

		// Add current position to map if not contained already.
		if(!map.containsKey(myPosition)) {
			map.put(myPosition, new HashSet<>());
		}

		HashSet<String> currentPositionNeighbors = map.get(myPosition);
		// For each discovered node
		for(int i = 1; i < lobs.size(); i++) {
			Couple<String,List<Attribute>> c = lobs.get(i);
			String nodeId = c.getLeft();

			HashSet<String> nodeNeighbors;
			if(!map.containsKey(nodeId)) {
				// If discovered for the first time, add unexplored node to map.
				nodeNeighbors = new HashSet<>();
				map.put(nodeId, nodeNeighbors);
				// if discovered for the first time, add to opened nodes.
				openedNodes.add(nodeId);
			} else {
				nodeNeighbors = map.get(nodeId);
			}
			nodeNeighbors.add(myPosition);
			currentPositionNeighbors.add(nodeId);
		}

		if (openedNodes.isEmpty()) {
			// If the set of opened nodes is empty, the agent's exploration can end.
			// Random Walk is initiated (transition 2).
//			System.out.println("Agent "+this.myAgent.getLocalName()+" terminated, list of visited nodes:");
//			System.out.println(map.entrySet());
//			System.out.println("Number of discovered nodes:" + map.size());
			this.transitionId = 1;
			return;

		}

		// next goal is first opened node in list
		int index = openedNodes.size()-1;
		String nextNode = openedNodes.get(index);
		this.explorationAgent.log("Currently opened:" + openedNodes);
		this.explorationAgent.log("Next goal is :" + nextNode);

		if(currentPositionNeighbors.contains(nextNode)) {
			// Remove opened node if reached
			openedNodes.remove(index);
			// set predecessor
			exploredNodes.put(nextNode, myPosition);
		} else {
			// Find path to next node
			// by backtracking
			nextNode = exploredNodes.get(myPosition);
		}

		try {
			this.explorationAgent.log("Moving to " + nextNode);
			this.explorationAgent.log("Press Enter in the console to allow the agent to execute its next move");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.transitionId = 0;
		// move to next node.
		// Move to the picked location. The move action (if any) MUST be the last action of your behaviour
		if(!this.explorationAgent.moveTo(nextNode)) {
			// obstacle, engage communication.
			this.getDataStore().put("exploration_blocked_notification", true);
			this.transitionId = 1;
		}
	}

	@Override
	public boolean done() {
		return this.transitionId != 0;
	}
	
	public int onEnd() {
		return this.transitionId;
	}
}
