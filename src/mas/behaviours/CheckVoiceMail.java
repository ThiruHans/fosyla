package mas.behaviours;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;
import java.util.List;

import mas.agents.ExplorationAgent;

public class CheckVoiceMail extends SimpleBehaviour {

	private static final long serialVersionUID = 8991919761504652388L;
	// transitionId: 
	// - 1 si Explore
	// - 2 si ShareData
	// - 3 si RequestStandby
	private int transitionId = 0;

	public CheckVoiceMail(ExplorationAgent explorationAgent) {
		super(explorationAgent);
	}

	@Override
	public void action() {
		ExplorationAgent agent = ((ExplorationAgent)this.myAgent);
		final MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		final ACLMessage msg = this.myAgent.receive(mt);
		this.transitionId = 1;

		if (msg != null) {

			// Add sender to agent recipients to share data in SendData behaviour.
			List<AID> recipientsList = (List<AID>)this.getDataStore().get("recipients_for_sharing");
			recipientsList.add(msg.getSender());

			agent.log("Message received in voicemail from: "+ msg.getSender());
			// Send acknowledgement to sender
			ACLMessage ack = new ACLMessage(ACLMessage.CONFIRM);
			ack.addReceiver(msg.getSender());
			ack.setSender(agent.getAID());
//			ack.setConversationId(agent.getAID() + ";" + msg.getSender());
			agent.sendMessage(ack);

			// Switch to SendData behaviour
			this.transitionId = 2;
		} else {
			// If no message was received and the agent checked the voicemail because of collision:
			// RequestStandby to nearby agents.
			Boolean blockedState = (boolean)this.getDataStore().get("exploration_blocked_notification");
			if (blockedState) {
				agent.log("No message was received but the agent was blocked");
				this.transitionId = 3;
				this.getDataStore().put("exploration_blocked_notification", false);
				return;
			}
			// Otherwise, just go back to exploring
			this.transitionId = 1;
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
