package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;

import mas.agents.ExploAgent;

public class CheckVoicemail extends SimpleBehaviour {

	private static final long serialVersionUID = 8991919761504652388L;
	// transitionId: 
	// - 1 si Explore
	// - 2 si ShareData
	// - 3 si RequestStandby
	private int transitionId = 0;

	public CheckVoicemail(ExploAgent exploAgent) {
		super(exploAgent);
	}

	@Override
	public void action() {
		ExploAgent agent = ((ExploAgent)this.myAgent);
		final MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		final ACLMessage msg = this.myAgent.receive(mt);
		this.transitionId = 1;
		if (msg != null) {
			// add sender to agent recipients to share data in SendData behaviour.
			agent.getRecipients().add(msg.getSender());
			// send acknowledgement
			ACLMessage ack = new ACLMessage(ACLMessage.CONFIRM);
			ack.addReceiver(msg.getSender());
//			ack.setConversationId(agent.getAID() + ";" + msg.getSender());
			agent.sendMessage(ack);
			
			this.transitionId = 2;
		} else {
			HashMap<String, Boolean> state = agent.getCurrentState();
			if (state.get("blocked")) {
				this.transitionId = 3;
				state.put("blocked", false);
			}
		}
	}

	@Override
	public boolean done() {
		return this.transitionId != 0;
	}
	
	public int onEnd() {
		return this.transitionId;
	}

}
