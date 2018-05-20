package mas.strategies;

import mas.agents.Agent;

public class MoveToTankerPosTankerStrategy extends Strategy {
    public MoveToTankerPosTankerStrategy(Agent agent) {
        super(agent);
    }

    @Override
    public void computeNewGoal() {
        String dest = (String)this.agent.getDataStore().get("tanker_position");
        this.agent.setNewDestination(dest);
    }
}
