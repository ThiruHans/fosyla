package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import mas.agents.AgentP;
import mas.agents.ExplorationAgent;

public class UpdatePOIs extends SimpleBehaviour {

    public static final int T_CHECK_VOICEMAIL = 10;

    private AgentP agent;

    public UpdatePOIs(AgentP agentP) {
        super(agentP);
        this.agent = agentP;
    }

    @Override
    public void action() {

    }

    @Override
    public boolean done() {
        return true;
    }

    public int onEnd() {
        return T_CHECK_VOICEMAIL;
    }
}
