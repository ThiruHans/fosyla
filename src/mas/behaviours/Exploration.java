package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import mas.agents.AgentP;
import mas.agents.ExplorationAgent;

import java.util.List;

public class Exploration extends SimpleBehaviour {

	private static final long serialVersionUID = 9088209402507795289L;
	private AgentP agent;
	private int transitionId = 0;

	public static final int T_CHECK_VOICEMAIL = 10;

	public Exploration(AgentP agentP) {
		super(agentP);
		this.agent = agentP;
	}

	@Override
	public void action() {
		this.agent.tick();
		// Get current position.
		String myPosition = this.agent.getCurrentPosition();
		// Get agent data
		List<String> openedNodes = this.agent.getOpenedNodes();

		// On startup, position may not be defined right away.
		if(myPosition.equals("")) return;

		this.agent.updateMap();

		if (openedNodes.isEmpty()) {
			// If the set of opened nodes is empty, the agent's exploration can end.
//			agent.log("Exploration finished: Switching to updatePOIs behaviour.");
			agent.log("Exploration finished: Switching to random walk behaviour.");

			this.getDataStore().put("default_movement_behaviour", ExplorationAgent.RANDOM_WALK);
			this.getDataStore().put("movement_behaviour", ExplorationAgent.RANDOM_WALK);
			return;

		}

		String nextNode;
		// If the agent has a planned path:
		List<String> plan = this.agent.getPlan();
		if(plan != null && plan.size() > 1) {
			// The next node is the next node in the plan.
			plan.remove(myPosition);
			nextNode = plan.get(plan.size()-1);
			this.agent.log("Path : " + plan);
		} else {
			this.agent.computePlan(myPosition);
			plan = this.agent.getPlan();
			nextNode = plan.get(plan.size() - 1);
		}
		this.agent.log("Next node is :" + nextNode);

		this.transitionId = T_CHECK_VOICEMAIL;
		block(200);

		// move to next node.
		// Move to the picked location. The move action (if any) MUST be the last action of your behaviour
		if(!this.agent.moveTo(nextNode)) {
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
