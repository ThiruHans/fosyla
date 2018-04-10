package mas.behaviours;

import env.Attribute;
import env.Couple;
import graph.Dijkstra;
import jade.core.behaviours.SimpleBehaviour;
import mas.agents.ExplorationAgent;
import utils.PointOfInterest;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Exploration extends SimpleBehaviour {

	private static final long serialVersionUID = 9088209402507795289L;
	private ExplorationAgent explorationAgent;
	private int transitionId = 0;

	public static final int T_CHECK_VOICEMAIL = 10;

	public Exploration(ExplorationAgent explorationAgent) {
		super(explorationAgent);
		this.explorationAgent = explorationAgent;
	}

	@Override
	public void action() {
		this.explorationAgent.tick();
		// Get current position.
		String myPosition = this.explorationAgent.getCurrentPosition();
		// Get agent data
		List<String> openedNodes = this.explorationAgent.getOpenedNodes();

		// On startup, position may not be defined right away.
		if(myPosition.equals("")) return;

		this.explorationAgent.updateMap();

		if (openedNodes.isEmpty()) {
			// If the set of opened nodes is empty, the agent's exploration can end.
//			explorationAgent.log("Exploration finished: Switching to updatePOIs behaviour.");
			explorationAgent.log("Exploration finished: Switching to random walk behaviour.");

			this.getDataStore().put("default_movement_behaviour", ExplorationAgent.RANDOM_WALK);
			this.getDataStore().put("movement_behaviour", ExplorationAgent.RANDOM_WALK);
			return;

		}

		String nextNode;
		// If the agent has a planned path:
		List<String> plan = this.explorationAgent.getPlan();
		if(plan != null && plan.size() > 1) {
			// The next node is the next node in the plan.
			plan.remove(myPosition);
			nextNode = plan.get(plan.size()-1);
			this.explorationAgent.log("Path : " + plan);
		} else {
			this.explorationAgent.computePlan(myPosition);
			plan = this.explorationAgent.getPlan();
			nextNode = plan.get(plan.size() - 1);
		}
		this.explorationAgent.log("Next node is :" + nextNode);

		this.transitionId = T_CHECK_VOICEMAIL;
		block(200);

		// move to next node.
		// Move to the picked location. The move action (if any) MUST be the last action of your behaviour
		if(!this.explorationAgent.moveTo(nextNode)) {
			// obstacle, engage communication.
			this.getDataStore().put("exploration_blocked_notification", true);
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
