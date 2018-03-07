package mas.behaviours;

import java.io.IOException;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import mas.agents.ExploAgent;
import utils.MessageContainer;

public class SendData extends SimpleBehaviour {
	private static final long serialVersionUID = -2619032417307731004L;
	private boolean finished = false;

	@Override
	public void action() {
		ExploAgent agent = ((ExploAgent)this.myAgent);
		
		ACLMessage datmsg = new ACLMessage(ACLMessage.INFORM);
		try {
			datmsg.setContentObject(new MessageContainer(agent.getMap(), agent.getOpenedNodes()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(AID aid : agent.getRecipients()) {
			datmsg.addReceiver(aid);
		}
		agent.getRecipients().clear();
		agent.sendMessage(datmsg);
		
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
