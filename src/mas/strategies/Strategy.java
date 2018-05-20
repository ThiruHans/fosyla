package mas.strategies;

import mas.agents.Agent;

public abstract class Strategy {
    protected Agent agent;

    Strategy(Agent agent) {
        this.agent = agent;
    }

    public abstract void computeNewGoal();
}
