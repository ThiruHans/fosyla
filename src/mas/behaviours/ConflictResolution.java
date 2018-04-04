package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import mas.agents.ExplorationAgent;
import utils.Priority;

public class ConflictResolution extends SimpleBehaviour {
    private ExplorationAgent agent;
    private boolean finished;

    public ConflictResolution(ExplorationAgent agent) {
        this.agent = agent;
    }

    @Override
    public void action() {
//        int priority = Priority.compute(this.agent, );
    }

    @Override
    public boolean done() {
        return false;
    }
}
