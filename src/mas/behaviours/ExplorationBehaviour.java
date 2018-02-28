package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import mas.agents.ExploAgent;
import env.Attribute;
import env.Couple;

public class ExplorationBehaviour extends SimpleBehaviour {
	/**
	 * When an agent choose to move
	 *  
	 */
	
	private List<String> openedNodes;
	private HashMap<String, String> exploredNodes;
	// transition: 
	// - 0 si boucle sur le meme comportement
	// - 1 si checkVoicemail
	// - 2 si requestStandby
	private int transitionId = 0;
	
	private static final long serialVersionUID = 9088209402507795289L;

	public ExplorationBehaviour (final mas.abstractAgent myagent) {
		super(myagent);
		
		ExploAgent agent = ((ExploAgent)this.myAgent);
		this.openedNodes = agent.getOpenedNodes();
		this.exploredNodes = agent.getExploredNodes();
	}

	@Override
	public void action() {
		//Example to retrieve the current position
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();
			
		if (myPosition != ""){
			//List of observable from the agent's current position
			List<Couple<String,List<Attribute>>> lobs=((mas.abstractAgent)this.myAgent).observe();//myPosition
			System.out.println(this.myAgent.getLocalName()+" -- list of observables: "+lobs);
			
			HashMap<String, HashSet<String>> map = ((ExploAgent)this.myAgent).getMap();
			// Add current position to map if not contained already.
			if(!map.containsKey(myPosition)) {				
				map.put(myPosition, new HashSet<String>());
			}
			
			HashSet<String> currentPositionNeighbors = map.get(myPosition);
			// For each discovered node
			for(int i = 1; i < lobs.size(); i++) {
				Couple<String,List<Attribute>> c = lobs.get(i);
				String nodeId = c.getLeft();
				// If discovered for the first time.
				if(!map.containsKey(nodeId)) {
					// add unexplored node to map.
					HashSet<String> neighbors = new HashSet<String>();
					neighbors.add(myPosition);
					map.put(nodeId, neighbors);
					
					// if discovered for the first time, add to opened nodes.
					this.openedNodes.add(nodeId);
				}
				currentPositionNeighbors.add(nodeId);
			}
			
			if (this.openedNodes.isEmpty()) {
				System.out.println("Agent terminated, list of visited nodes:");
				System.out.println(map.entrySet());
				System.out.println("Number of discovered nodes:" + map.size());
				this.;
				return;
			}
					
			// next node is first opened node in list
			int index = this.openedNodes.size()-1;
			String nextNode = this.openedNodes.get(index);
			System.out.println("Currently opened:" + this.openedNodes);
			System.out.println("Next goal is :" + nextNode);
			
			if(currentPositionNeighbors.contains(nextNode)) {
				// Remove opened node if reached
				this.openedNodes.remove(index);
				// set predecessor
				this.exploredNodes.put(nextNode, myPosition);
			} else {
				// Find path to next node
				nextNode = this.exploredNodes.get(myPosition);
			}
			
			try {
				System.out.println("Moving to " + nextNode);
				System.out.println("Press Enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// move to next node.
			// 2) Move to the picked location. The move action (if any) MUST be the last action of your behaviour
			if(!((mas.abstractAgent)this.myAgent).moveTo(nextNode)) {
				// obstacle rencontré, essayer de communiquer
				this.finished = true;
			}
		}

	}

	@Override
	public boolean done() {
		return this.finished;
	}
	
	public int onEnd() {
		return 1;
	}
}
