package mas.behaviours;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import mas.agents.ExplorationAgent;
import utils.MapDataContainer;

public class RcvData extends SimpleBehaviour {
	private static final long serialVersionUID = -1268869791618955047L;
	private int attempts = 0;

	public RcvData(ExplorationAgent explorationAgent) {
		super(explorationAgent);
	}

	@Override
	public void action() {
		attempts += 1;
		final MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		final ACLMessage msg = this.myAgent.receive(mt);
		ExplorationAgent agent = ((ExplorationAgent)this.myAgent);
		
	 	if (msg != null) {
	 		agent.log("Data received, combining...");
	 		MapDataContainer mc;
			try {
				mc = (MapDataContainer)msg.getContentObject();
				HashMap<String, HashSet<String>> otherMap = mc.getMap();
				HashMap<String, HashSet<String>> map = agent.getMap();
				List<String> openedNodes = agent.getOpenedNodes();
				
				for(String e : otherMap.keySet()) {
					if(!map.containsKey(e)) map.put(e, otherMap.get(e));
					if(openedNodes.contains(e)) openedNodes.remove(e);
				}
			attempts += 1;	
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
	 	} else {
	 		block(500);
			agent.log("No data received yet, waiting for one second.");
	 	}
	}

	@Override
	public boolean done() {
		return attempts > 1;
	}

	public int onEnd() {
		return 1;
	}

}
