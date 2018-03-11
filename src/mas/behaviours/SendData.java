package mas.behaviours;

import java.io.IOException;
import java.util.List;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import mas.agents.ExplorationAgent;
import utils.MapDataContainer;

public class SendData extends SimpleBehaviour {
	private static final long serialVersionUID = -2619032417307731004L;
	private boolean finished = false;

	public SendData(ExplorationAgent explorationAgent) {
		super(explorationAgent);
	}

	@Override
	public void action() {
		ExplorationAgent agent = ((ExplorationAgent)this.myAgent);
		
		ACLMessage dataMessage = new ACLMessage(ACLMessage.INFORM);
		dataMessage.setSender(agent.getAID());
		try {
			dataMessage.setContentObject(new MapDataContainer(agent.getMap(), agent.getOpenedNodes()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<AID> recipients = (List<AID>)this.getDataStore().get("recipients_for_sharing");
		for(AID aid : recipients) {
			dataMessage.addReceiver(aid);
		}
		agent.log("Send data to "+ recipients);
		recipients.clear();
		agent.sendMessage(dataMessage);
		this.finished = true;
	}

	@Override
	public boolean done() {
		return this.finished;
	}
	
	public int onEnd() {
		return 1;
	}

}
