package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import mas.agents.ExplorationAgent;


public class ConflictResolution extends SimpleBehaviour {
    private ExplorationAgent agent;
    private boolean finished;

    public ConflictResolution(ExplorationAgent agent) {
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
