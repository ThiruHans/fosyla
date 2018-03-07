package mas.agents;


import jade.core.AID;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import mas.abstractAgent;
import mas.behaviours.CheckVoicemail;
import mas.behaviours.ExplorationBehaviour;
import mas.behaviours.RequestStandby;
import env.Environment;


public class ExploAgent extends abstractAgent{

	private static final long serialVersionUID = -1784844593772918359L;
	
	private HashMap<String, HashSet<String>> map;
	private List<String> openedNodes;
	private HashMap<String, String> exploredNodes;
	
	private HashMap<String, Boolean> currentState;
	private List<AID> recipients;
	
	protected void setup() {

		super.setup();

		//get the parameters given into the object[]. In the current case, the environment where the agent will evolve
		final Object[] args = getArguments();
		if(args[0]!=null){
			deployAgent((Environment) args[0]);
		}else{
			System.err.println("Malfunction during parameter's loading of agent"+ this.getClass().getName());
			System.exit(-1);
		}
		
		this.map = new HashMap<>();
		this.exploredNodes = new HashMap<>();
		this.openedNodes = new ArrayList<>();
		
		// Inscription aux pages jaunes
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd  = new ServiceDescription();
		sd.setType("explorer");
		sd.setName(getLocalName());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd );
		} catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}
		
		// Init state
		this.currentState.put("blocked", false);
		
		//Add the behaviours
		FSMBehaviour fsm = new FSMBehaviour();
		fsm.registerFirstState(new ExplorationBehaviour(this), "Explore");
		fsm.registerState(new CheckVoicemail(this), "CheckVoicemail");
		fsm.registerState(new RequestStandby(this), "RequestStandby");
		// fsm.
		
		fsm.registerTransition("Explore", "RequestStandby", 1);

		System.out.println("the agent "+this.getLocalName()+ " is started");
	}
	
	public HashMap<String, HashSet<String>> getMap() {
		return this.map;
	}
	
	public List<String> getOpenedNodes() {
		return openedNodes;
	}

	public HashMap<String, String> getExploredNodes() {
		return exploredNodes;
	}

	public HashMap<String, Boolean> getCurrentState() {
		return currentState;
	}

	public List<AID> getRecipients() {
		return recipients;
	}
	
}
