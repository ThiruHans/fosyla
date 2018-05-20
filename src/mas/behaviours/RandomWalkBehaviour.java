package mas.behaviours;

import env.Attribute;
import env.Couple;
import mas.agents.Agent;

import java.util.List;

public class RandomWalkBehaviour extends ABehaviour {

	private int step;
	private int max_steps;

	public RandomWalkBehaviour (Agent agent) {
		super(agent);
		this.step = 0;
	}

	@Override
	public void action() {
		if (this.agent.getDestination() != null) this.agent.setNewDestination(null);
		if (this.step == 0)
			this.max_steps = (int)this.getDataStore().get("random_walk_max_steps");

		String position = this.agent.getCurrentPosition();
		block(Agent.TIME_STEP);
		this.step += 1;

		List<Couple<String, List<Attribute>>> lobs = this.agent.observe();
		this.agent.updateMap();
		int nextNodeIdx = this.agent.getRandom().nextInt(lobs.size());
		while (lobs.get(nextNodeIdx).getLeft().equals(position)) {
			nextNodeIdx = this.agent.getRandom().nextInt(lobs.size());
		}

		this.agent.log("Random Walk, Step=" + this.step + ", MaxStep="+this.max_steps);
		this.agent.moveTo(lobs.get(nextNodeIdx).getLeft());
	}

	@Override
	public boolean done() {
		return this.step >= this.max_steps;
	}

	public int onEnd() {
		this.step = 0;
		this.getDataStore().put("random_walk", false);
		this.getDataStore().put("random_walk_max_steps", 0);
		this.getDataStore().put("avoiding_conflict", false);
		this.agent.emptyMessages();
		return Agent.A_CHECK_VOICEMAIL;
	}
}