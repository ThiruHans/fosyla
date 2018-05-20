package mas.behaviours;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.agents.Agent;

import java.util.List;

public class CheckVoiceMail extends ABehaviour {
    private int transition;

    public CheckVoiceMail(Agent agent) {
        super(agent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage msg = this.agent.receive(mt);

        Boolean blockedState = (boolean) this.getDataStore().get("block_notification");
//        if (blockedState) {
//            this.getDataStore().put("block_number", (int)this.getDataStore().get("block_number")+1);
//            if ((int)(this.getDataStore().get("block_number")) >= 2) {
//                this.agent.log("Blocked two times on same node");
//                this.getDataStore().put("walk_to_random", true);
//                this.getDataStore().put("walk_to_random_max_steps", 1);
//                this.transition = this.agent.getMovementBehaviour();
//                return;
//            }
//        }

        if (msg != null) {
            List<AID> recipientsList = (List<AID>)
                    this.getDataStore().get("recipients_for_sharing");
            recipientsList.add(msg.getSender());

            this.agent.log("Message received in voice mail from: "
                    + msg.getSender().getLocalName());
            ACLMessage ack = new ACLMessage(ACLMessage.CONFIRM);
            ack.addReceiver(msg.getSender());
            ack.setSender(this.agent.getAID());
            this.agent.sendMessage(ack);

            this.transition = Agent.A_SEND_DATA;

            // Empty voice mail
            while (this.agent.receive(mt) != null);
            return;
        }

        if (blockedState) {
            this.agent.log("No message was received but the agent was blocked.");
            this.transition = Agent.A_REQUEST_STANDBY;
            return;
        }
        // If no event, go back to default function
        this.transition = this.agent.getMovementBehaviour();
    }

    @Override
    public boolean done() {
        return true;
    }

    public int onEnd() {
        this.getDataStore().put("block_notification", false);
        return transition;
    }
}
