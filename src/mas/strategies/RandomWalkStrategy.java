package mas.strategies;

import mas.agents.Agent;

public class RandomWalkStrategy extends Strategy {

    public RandomWalkStrategy(Agent agent) {
        super(agent);
    }

    @Override
    public void computeNewGoal() {
        this.agent.setNewDestination(null);
    }
}
