package mas.behaviours;

import env.EntityType;
import jade.core.AID;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import mas.agents.Agent;

import java.io.IOException;
import java.util.List;

public class SendDataBehaviour extends ABehaviour {

    public SendDataBehaviour(Agent agent) {
        super(agent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setSender(this.agent.getAID());
        try {
            DataStore messageContainer = new DataStore();
            messageContainer.put("updater_name", this.getDataStore().get("updater_name"));
            messageContainer.put("position", this.agent.getCurrentPosition());
            messageContainer.put("map", this.agent.getMap());
            messageContainer.put("points", this.agent.getPoints());
            messageContainer.put("tanker_position", this.getDataStore().get("tanker_position"));
            messageContainer.put("tanker_position_date", this.getDataStore().get("tanker_position_date"));
            messageContainer.put("type", this.agent.getType());

            if (this.agent.getType() != EntityType.AGENT_EXPLORER) {
                messageContainer.put("opened_nodes", this.agent.getOpenedNodes());
            }

            msg.setContentObject(messageContainer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<AID> recipients = (List<AID>)this.getDataStore().get("recipients_for_sharing");
        for (AID aid : recipients) msg.addReceiver(aid);
        this.agent.log("Send data to " + recipients);
        recipients.clear();
        this.agent.sendMessage(msg);
    }

    @Override
    public boolean done() {
        return true;
    }

    public int onEnd() {
        return Agent.A_RCV_DATA;
    }
}
