package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import mas.agents.AgentP;
import mas.agents.ExplorationAgent;


public class ConflictResolution extends SimpleBehaviour {
    private AgentP agent;
    private boolean finished;

    public ConflictResolution(AgentP agent) {
        this.agent = agent;
    }

    @Override
    public void action() {

    }

    @Override
    public boolean done() {
        return false;
    }
}
