package mas.agents;

import env.Attribute;
import env.Couple;
import env.EntityType;
import env.Environment;
import graph.Dijkstra;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import mas.abstractAgent;
import mas.behaviours.*;
import utils.PointOfInterest;

import java.util.*;

public class ExplorationAgent extends AgentP {
	/**
	 * Exploration Agent:
	 * Explores the graph it is dropped into. If another agent is met, current knowledge of the map is shared
	 * and coordination for the remaining exploration is computed.
	 */

	private static final long serialVersionUID = -1784844593772918359L;

	protected void setup() {

		super.setup();
		
		// Init state
		// The exploration agent provides a `dataStore` to some behaviours to allow the passing of data between state
		// in the FSM.
		dataStore.put("exploration_blocked_notification", false);
		dataStore.put("recipients_for_sharing", new ArrayList<>());
		dataStore.put("movement_behaviour", ExplorationAgent.T_EXPLORE);
		dataStore.put("default_movement_behaviour", ExplorationAgent.T_EXPLORE);

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
		UpdatePOIs updatePOIsBehaviour = new UpdatePOIs(this);

		// Common data store.
		explorationBehaviour.setDataStore(dataStore);
		checkVoiceMailBehaviour.setDataStore(dataStore);
		sendDataBehaviour.setDataStore(dataStore);
		waitForStandbyBehaviour.setDataStore(dataStore);
		rcvDataBehaviour.setDataStore(dataStore);
		sendGoalBehaviour.setDataStore(dataStore);
		rcvGoalBehaviour.setDataStore(dataStore);
		randomWalkBehaviour.setDataStore(dataStore);

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

		// Register all transitions.
		fsm.registerTransition("Explore", "CheckVoiceMail", Exploration.T_CHECK_VOICEMAIL);
		fsm.registerTransition("CheckVoiceMail", "Explore", ExplorationAgent.T_EXPLORE);
		fsm.registerTransition("CheckVoiceMail", "RandomWalk", ExplorationAgent.RANDOM_WALK);

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
}
