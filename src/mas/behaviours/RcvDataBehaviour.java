package mas.behaviours;

import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import mas.agents.Agent;

public class RcvDataBehaviour extends ABehaviour {
    private int transition;

    public RcvDataBehaviour(Agent agent) {
        super(agent);
    }

    @Override

    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage msg = this.agent.blockingReceive(mt, Agent.WAIT_TIME);

        if (msg != null) {
            this.agent.log("Data received, processing...");
            try {
                DataStore ds = (DataStore) msg.getContentObject();

                this.agent.newDataReceived(ds);

                this.getDataStore().put("aid_for_goal", msg.getSender());
                this.transition = Agent.A_SEND_GOAL;
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            // empty mail
            while(this.agent.receive(mt) != null);
            return;
        }

        this.transition = Agent.A_CHECK_VOICEMAIL;
        this.agent.log("No data received...");
    }

    @Override
    public boolean done() {
        return true;
    }

    public int onEnd() {
        return this.transition;
    }
}
