package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import mas.agents.Agent;

abstract class ABehaviour extends SimpleBehaviour {
    protected Agent agent;

    ABehaviour(Agent agent) {
        super(agent);
        this.agent = agent;
    }
}
