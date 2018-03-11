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
	private boolean finished = false;

	public RcvData(ExplorationAgent explorationAgent) {
		super(explorationAgent);
	}

	@Override
	public void action() {
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
				
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
	 	} else {
	 		block(1000);
			agent.log("No data received yet, waiting for one second.");
	 	}
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
