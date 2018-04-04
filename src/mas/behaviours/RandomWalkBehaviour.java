package mas.behaviours;

import env.Attribute;
import env.Couple;
import jade.core.behaviours.SimpleBehaviour;
import mas.agents.ExplorationAgent;

import java.util.List;
import java.util.Random;



public class RandomWalkBehaviour extends SimpleBehaviour {
	/**
	 * When an agent choose to move
	 *  
	 */
	private static final long serialVersionUID = 9088209402507795289L;
	private int maxSteps = 10;
	private int step = 0;

	public RandomWalkBehaviour (final mas.abstractAgent myagent) {
		super(myagent);
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

			//Random move from the current position
			Random r= new Random();
			//1) get a couple <Node ID,list of percepts> from the list of observables
			int moveId=r.nextInt(lobs.size());

			((ExplorationAgent)this.myAgent).log("RandomWalk| Step = " + this.step);

			//2) Move to the picked location. The move action (if any) MUST be the last action of your behaviour
			((mas.abstractAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
		}

	}

	public boolean done() {
		((ExplorationAgent)this.myAgent).log("RandomWalk| DONE");
		return this.step >= this.maxSteps;
	}

	public int onEnd() {
		this.getDataStore().put("movement_behaviour", "exploration");

		return 1;
	}

}