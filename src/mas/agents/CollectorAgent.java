package mas.agents;

import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;

import java.util.ArrayList;
import java.util.Collections;

import mas.behaviours.CheckVoiceMail;
import mas.behaviours.Collection;
import mas.behaviours.Exploration;
import mas.behaviours.RandomWalk;
import mas.behaviours.RcvData;
import mas.behaviours.RcvGoal;
import mas.behaviours.RequestStandby;
import mas.behaviours.SendData;
import mas.behaviours.SendGoal;
import mas.behaviours.WaitForStandby;
import utils.PointOfInterest;
import env.Attribute;

public class CollectorAgent extends AgentP{

	private static final long serialVersionUID = -5739158419466940705L;

	protected void setup() {

		super.setup();
		
		// Initialize state
		// The exploration agent provides a `dataStore` to some behaviors to allow the passing of data between state
		// in the FSM.
		DataStore dataStore = new DataStore();
		dataStore.put("exploration_blocked_notification", false);
		dataStore.put("recipients_for_sharing", new ArrayList<>());
		dataStore.put("movement_behaviour", CollectorAgent.T_EXPLORE);
		dataStore.put("default_movement_behaviour", CollectorAgent.T_EXPLORE);

		// Initialize behaviors
		Exploration explorationBehaviour = new Exploration(this);
		CheckVoiceMail checkVoiceMailBehaviour = new CheckVoiceMail(this);
		SendData sendDataBehaviour = new SendData(this);
		RcvData rcvDataBehaviour = new RcvData(this);
		RequestStandby requestStandbyBehaviour = new RequestStandby(this);
		WaitForStandby waitForStandbyBehaviour = new WaitForStandby(this);
		RandomWalk randomWalkBehaviour = new RandomWalk(this);
		SendGoal sendGoalBehaviour = new SendGoal(this);
		RcvGoal rcvGoalBehaviour = new RcvGoal(this);
		Collection collectionBehaviour = new Collection(this);
//		UpdatePOIs updatePOIsBehaviour = new UpdatePOIs(this);

		// Common data store.
		explorationBehaviour.setDataStore(dataStore);
		checkVoiceMailBehaviour.setDataStore(dataStore);
		sendDataBehaviour.setDataStore(dataStore);
		waitForStandbyBehaviour.setDataStore(dataStore);
		rcvDataBehaviour.setDataStore(dataStore);
		sendGoalBehaviour.setDataStore(dataStore);
		rcvGoalBehaviour.setDataStore(dataStore);
		randomWalkBehaviour.setDataStore(dataStore);
		collectionBehaviour.setDataStore(dataStore);

		// Add the behaviors to the Finite State Machine.
		FSMBehaviour fsm = new FSMBehaviour();
		fsm.registerFirstState(explorationBehaviour, "Explore");
		fsm.registerState(checkVoiceMailBehaviour, "CheckVoiceMail");
		fsm.registerState(requestStandbyBehaviour, "RequestStandby");
		fsm.registerState(waitForStandbyBehaviour, "WaitForStandby");
		fsm.registerState(sendDataBehaviour, "SendData");
		fsm.registerState(rcvDataBehaviour, "RcvData");
		fsm.registerState(randomWalkBehaviour, "RandomWalk");
		fsm.registerState(sendGoalBehaviour, "SendGoal");
		fsm.registerState(rcvGoalBehaviour, "RcvGoal");
		fsm.registerState(collectionBehaviour, "Collection");

		// Register all transitions.
		fsm.registerTransition("Explore", "CheckVoiceMail", Exploration.T_CHECK_VOICEMAIL);
		fsm.registerTransition("CheckVoiceMail", "Explore", CollectorAgent.T_EXPLORE);
		fsm.registerTransition("CheckVoiceMail", "RandomWalk", CollectorAgent.RANDOM_WALK); // fin exploration
		fsm.registerTransition("CheckVoiceMail", "Collection", CollectorAgent.COLLECTION);
		fsm.registerTransition("Collection", "CheckVoiceMail", Collection.T_CHECK_VOICEMAIL);
		fsm.registerTransition("CheckVoiceMail", "SendData", CheckVoiceMail.T_SEND_DATA);
		fsm.registerTransition("CheckVoiceMail", "RequestStandby", CheckVoiceMail.T_REQUEST_STANDBY);
		fsm.registerTransition("SendData", "RcvData", SendData.T_RCV_DATA);
		fsm.registerTransition("RcvData", "SendGoal", RcvData.T_SEND_GOAL);
		fsm.registerTransition("RcvData", "CheckVoiceMail", RcvData.T_CHECK_VOICEMAIL);
		fsm.registerTransition("SendGoal", "RcvGoal", SendGoal.T_RCV_GOAL);
		fsm.registerTransition("RcvGoal", "CheckVoiceMail", RcvGoal.T_CHECK_VOICEMAIL);

		fsm.registerTransition("RequestStandby", "WaitForStandby", RequestStandby.T_WAIT_FOR_STANDBY);
		fsm.registerTransition("WaitForStandby", "SendData", WaitForStandby.T_SEND_DATA);
		fsm.registerTransition("WaitForStandby", "CheckVoiceMail", WaitForStandby.T_CHECK_VOICEMAIL);

		fsm.registerTransition("RandomWalk", "CheckVoiceMail", RandomWalk.T_CHECK_VOICEMAIL);

		addBehaviour(fsm);
		System.out.println("the agent "+this.getLocalName()+" is started");
	}
	
	@Override
    public void computePlan(String position) {
        String goal = "";
        PointOfInterest bestpoi = null;
        int maxValue = Integer.MIN_VALUE;
        String agentTreasureType = this.getMyTreasureType();
        if (!this.pois.isEmpty()){
        	for(PointOfInterest p: pois){
        		for(Attribute attr: p.getAttrs()){
        			if(attr.getName().equals(agentTreasureType) && (int) attr.getValue() > maxValue){
        				bestpoi = p;
        				maxValue = (int) attr.getValue();
        			}
        		}
        	}
        	
        }
        if(bestpoi == null){
    		if (!openedNodes.isEmpty()) {
    			goal = openedNodes.get(openedNodes.size()-1);
    			dataStore.put("movement_behaviour", AgentP.T_EXPLORE);
    			dataStore.put("default_movement_behaviour", AgentP.T_EXPLORE);
    		} else {
    			dataStore.put("movement_behaviour", AgentP.RANDOM_WALK);
    			dataStore.put("default_movement_behaviour", AgentP.COLLECTION);
    		}
    	} else {
    		goal = bestpoi.getNode();
    		dataStore.put("movement_behaviour", AgentP.COLLECTION);
			dataStore.put("default_movement_behaviour", AgentP.COLLECTION);
    	}
        
        if (goal.equals("")) {
        	this.currentPlan = Collections.emptyList();
        	return;
        }
        // Next goal is next unexplored node in `openedNodes`.
        // Compute shortest path to goal with Dijkstra:
        this.dijkstra.computeShortestPaths(position);
        this.log("New goal : " + goal + ". Currently opened:" + openedNodes + ". NDiscovered="+map.size());
        this.currentPlan = this.dijkstra.getPath(position, goal);
        this.log("Path : " + this.currentPlan);
//		this.log("MAP:" + map);
    }

}
