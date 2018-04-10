package mas.behaviours;

import env.Attribute;
import env.Couple;
import jade.core.behaviours.SimpleBehaviour;
import mas.agents.ExplorationAgent;

import java.util.List;
import java.util.Random;



public class RandomWalk extends SimpleBehaviour {

	private static final long serialVersionUID = 9088209402507795289L;
	public static final int T_CHECK_VOICEMAIL = 10;
	private static final int MAX_STEPS = 5;
	private int step = 0;
	private Random random;

	public RandomWalk(final mas.abstractAgent myagent) {
		super(myagent);
		this.random = ((ExplorationAgent)myagent).getRandomGenerator();
	}

	@Override
	public void action() {
		//Example to retrieve the current position
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();

		block(200);

		if (!myPosition.equals("")){
			this.step += 1;

			//List of observable from the agent's current position
			List<Couple<String,List<Attribute>>> lobs=((mas.abstractAgent)this.myAgent).observe();//myPosition

			((ExplorationAgent)this.myAgent).updateMap();

			//1) get a couple <Node ID,list of percepts> from the list of observables
			int moveId=this.random.nextInt(lobs.size());
			while (lobs.get(moveId).getLeft().equals(myPosition)) {
				moveId = this.random.nextInt(lobs.size());
			}

			((ExplorationAgent)this.myAgent).log("|RandomWalk| Step = " + this.step);

			//2) Move to the picked location. The move action (if any) MUST be the last action of your behaviour
			if(!((mas.abstractAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft())) {
				this.getDataStore().put("exploration_blocked_notification", true);
			}
		}
	}

	public boolean done() {
		return this.step >= MAX_STEPS;
	}

	public int onEnd() {
		this.getDataStore().put("movement_behaviour", this.getDataStore().get("default_movement_behaviour"));
		this.step = 0;
		return T_CHECK_VOICEMAIL;
	}

}