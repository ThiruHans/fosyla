package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import mas.agents.AgentP;
import mas.agents.ExplorationAgent;
import utils.MessageContainer;

import java.util.List;

public class RcvGoal extends SimpleBehaviour {

    private int attempts;
    public static final int CONFLICT_RESOLUTION = 10;
    public static final int T_CHECK_VOICEMAIL = 11;

    public RcvGoal(AgentP agentP) {
        super(agentP);
        this.attempts = 0;
    }

    @Override
    public void action() {
        attempts += 1;
        final MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
        final ACLMessage msg = this.myAgent.receive(mt);
        AgentP agent = ((AgentP)this.myAgent);

        if (msg != null) {
            agent.log("Goal received, processing...");
            MessageContainer mc;
            try {
                mc = (MessageContainer) msg.getContentObject();
                List<String> otherPath = mc.getCurrentPath();
                List<String> ownPath = agent.getPlan();

                // detect conflict


                attempts += 1;
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        } else {
            block(500);
            agent.log("No goal received yet, waiting for 500ms.");
        }
    }

    @Override
    public boolean done() {
        return attempts > 1;
    }

    public int onEnd() {
        attempts = 0;
        this.getDataStore().put("movement_behaviour", ExplorationAgent.RANDOM_WALK);
        return T_CHECK_VOICEMAIL;
    }
}
