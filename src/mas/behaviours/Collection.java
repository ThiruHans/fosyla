package mas.behaviours;

import java.util.List;

import env.Attribute;
import env.Couple;
import mas.agents.AgentP;
import mas.agents.CollectorAgent;
import jade.core.behaviours.SimpleBehaviour;

public class Collection extends SimpleBehaviour {

	public static final int T_CHECK_VOICEMAIL = 10;
	private CollectorAgent agent;

	public Collection(CollectorAgent a) {
		super(a);
		agent = a;
	}

	@Override
	public void action() {
		this.agent.tick();
		// Get current position.
		String myPosition = this.agent.getCurrentPosition();

		if (myPosition.equals(this.agent.getGoalPoi().getNode())) {
			int quantity = this.agent.pick();
			this.agent.log("Collected " + quantity + " "
					+ this.agent.getMyTreasureType());
			this.agent.resetGoalPoi();
		}

		this.agent.updateMap();
		
		if(this.agent.getBackPackFreeSpace() == 0) {
			this.getDataStore().put("movement_behaviour", AgentP.RANDOM_WALK);
			this.getDataStore().put("default_movement_behaviour", AgentP.RANDOM_WALK);
			this.agent.resetPlan();
			return;
		}

		String nextNode;
		// If the agent has a planned path:
		List<String> plan = this.agent.getPlan();
		if (plan != null && plan.size() > 1) {
			// The next node is the next node in the plan.
			plan.remove(myPosition);
			nextNode = plan.get(plan.size() - 1);
			this.agent.log("Path : " + plan);
		} else {
			this.agent.log("Computing plan");
			this.agent.computePlan(myPosition);
			plan = this.agent.getPlan();
			nextNode = plan.get(plan.size() - 1);
		}
		this.agent.log("Next node is :" + nextNode);

		if (!this.agent.moveTo(nextNode)) {
			// obstacle, engage communication.
			this.agent.log("Blocked");
			this.getDataStore().put("exploration_blocked_notification", true);
		}

		block(500);

		// List<Couple<String,List<Attribute>>> lobs = this.agent.observe();
		// for (Couple observable: lobs) {
		// String node = (String) observable.getLeft();
		// List<Attribute> attrs = (List<Attribute>) observable.getRight();
		// if(node.equals(this.agent.getCurrentPosition())){
		// String agentTreasureType = this.agent.getMyTreasureType();
		// if(!attrs.isEmpty()){
		// for(Attribute attr: attrs){
		// if(attr.getName().equals(agentTreasureType)){
		// int quantity = this.agent.pick();
		// break;
		// }
		// }
		// }
		// }
		// }

	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return true;
	}

	public int onEnd() {
		return T_CHECK_VOICEMAIL;
	}

}
