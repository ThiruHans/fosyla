package mas.behaviours;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.agents.Agent;

import java.util.List;

public class WaitForStandbyBehaviour extends ABehaviour {

    private int transition;

    public WaitForStandbyBehaviour(Agent agent) {
        super(agent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
        ACLMessage msg = this.agent.blockingReceive(mt, Agent.WAIT_TIME);

        if (msg != null) {
            this.agent.log("WaitForStandby: Ack received");
            // add ack sender to recipients for SendData behavior
            ((List<AID>)this.getDataStore().get("recipients_for_sharing"))
                    .add(msg.getSender());
            this.transition = Agent.A_SEND_DATA;
            return;
        }

        this.agent.log("WaitForStandby: No ack received");
        this.transition = Agent.A_CHECK_VOICEMAIL;
    }

    @Override
    public boolean done() {
        return true;
    }

    public int onEnd() {
        return this.transition;
    }
}
