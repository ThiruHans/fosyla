package mas.behaviours;

import mas.agents.ExploAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CheckVoicemail extends SimpleBehaviour {

	private static final long serialVersionUID = 8991919761504652388L;
	private int transitionId = 0;

	public CheckVoicemail(ExploAgent exploAgent) {
		super(exploAgent);
	}

	@Override
	public void action() {
		final MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		final ACLMessage msg = this.myAgent.receive(mt);
		this.transitionId = 1;
		if (msg != null) {
			// send acknowledgement
			ACLMessage ack = new ACLMessage(ACLMessage.CONFIRM);
			ack.addReceiver(msg.getSender());
			ack.setConversationId(this.myAgent.getAID() + ";" + msg.getSender());
			((mas.abstractAgent)this.myAgent).sendMessage(ack);
			
			this.transitionId = 2;
		}
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return this.transitionId != 0;
	}
	
	public int onEnd() {
		return this.transitionId;
	}

}
