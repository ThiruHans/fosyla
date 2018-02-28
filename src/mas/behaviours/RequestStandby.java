package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import mas.agents.ExploAgent;

public class RequestStandby extends SimpleBehaviour {

	private static final long serialVersionUID = -5079787101111370695L;
	
	private boolean finished = false;

	public RequestStandby(ExploAgent agent) {
		super(agent);
	}

	@Override
	public void action() {
		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();
		
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setSender(this.myAgent.getAID());
		
		// get potential receivers
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd  = new ServiceDescription();
		sd.setType( "explorer" ); 
		dfd.addServices(sd);
		DFAgentDescription[] results;
		
		try {
			results = DFService.search(this.myAgent, dfd);
			if (results.length>0) {
				for(DFAgentDescription d : results) {
					msg.addReceiver(d.getName());
				}
			}
		} catch (FIPAException e) {
			e.printStackTrace();
			return;
		}
		
		msg.setContent(myPosition);
		((mas.abstractAgent)this.myAgent).sendMessage(msg);
	}

	@Override
	public boolean done() {
		return this.finished;
	}
	
	public int onEnd() {
		return 1;
	}

}
