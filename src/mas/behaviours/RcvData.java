package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import mas.agents.ExploAgent;
import utils.MessageContainer;

public class RcvData extends SimpleBehaviour {
	private static final long serialVersionUID = -1268869791618955047L;
	private boolean finished = false;

	@Override
	public void action() {
		final MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		final ACLMessage msg = this.myAgent.receive(mt);
		ExploAgent agent = ((ExploAgent)this.myAgent);
		
	 	if (msg != null) {
	 		MessageContainer mc;
			try {
				mc = (MessageContainer)msg.getContentObject();
				HashMap<String, HashSet<String>> otherMap = mc.getMap();
				HashMap<String, HashSet<String>> map = agent.getMap();
				List<String> openedNodes = agent.getOpenedNodes();
				
				for(String e : otherMap.keySet()) {
					if(!map.containsKey(e)) map.put(e, otherMap.get(e));
					if(openedNodes.contains(e)) openedNodes.remove(e);
				}
				
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			this.finished = true;
	 	} else {
	 		block(500);
	 	}
	}

	@Override
	public boolean done() {
		return this.finished;
	}

}
