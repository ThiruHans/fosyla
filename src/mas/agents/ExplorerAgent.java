package mas.agents;

import env.EntityType;
import jade.core.AID;
import jade.core.behaviours.DataStore;
import mas.behaviours.UpdatePOIBehaviour;
import mas.strategies.UpdatePOIStrategy;

public class ExplorerAgent extends Agent {

    public void setup() {
        super.setup();

        strategies.put(M_UPDATE_POI, new UpdatePOIStrategy(this));

        UpdatePOIBehaviour updatePOIBehaviour = new UpdatePOIBehaviour(this);
        updatePOIBehaviour.setDataStore(this.dataStore);

        fsm.registerState(updatePOIBehaviour, "UpdatePOI");
        fsm.registerTransition("UpdatePOI", "CheckVoiceMail", A_CHECK_VOICEMAIL);
        fsm.registerTransition("CheckVoiceMail", "UpdatePOI", M_UPDATE_POI);
    }

    @Override
    public int getMovementBehaviour() {
//        if ((boolean) this.dataStore.get("random_walk"))
//            return M_RANDOM_WALK;
//
//        if ((boolean) this.dataStore.get("avoiding_conflict"))
//            return M_AVOID_CONFLICT;
//
//        if ((boolean) this.dataStore.get("walk_to_random"))
//            return M_WALK_TO_RANDOM;
//
//        if (this.openedNodes.isEmpty()) {
//            if (this.points.isEmpty()) {
//                this.dataStore.put("random_walk_max_steps", 1);
//                return M_RANDOM_WALK;
//            } else {
//                if (this.dataStore.get("updater_name").equals(this.getLocalName())
//                        && this.getPoints().size() > 1) {
//                    return M_UPDATE_POI;
////                    this.dataStore.put("walk_to_random_max_steps", 5);
////                    return M_WALK_TO_RANDOM;
//                } else {
////                    this.dataStore.put("walk_to_random_max_steps", 5);
//                    this.dataStore.put("random_walk_max_steps", 1);
//                    return M_RANDOM_WALK;
////                    return M_WALK_TO_RANDOM;
//                }
//            }
//        }
//        else
//            return M_EXPLORE;
        if ((boolean) this.dataStore.get("random_walk"))
            return M_RANDOM_WALK;

        if ((boolean) this.dataStore.get("avoiding_conflict"))
            return M_AVOID_CONFLICT;

        if ((boolean) this.dataStore.get("walk_to_random"))
            return M_WALK_TO_RANDOM;

        if (this.openedNodes.isEmpty()) {
            if (this.points.isEmpty()) {
                this.dataStore.put("random_walk_max_steps", 1);
                return M_RANDOM_WALK;
            } else {
                if (this.dataStore.get("updater_name").equals(this.getLocalName())) {
                    return M_UPDATE_POI;
                } else {
                    this.dataStore.put("walk_to_random", true);
                    this.dataStore.put("walk_to_random_max_steps", 1);
//                    return M_RANDOM_WALK;
                    return M_WALK_TO_RANDOM;
                }
            }
        } else {
            return M_EXPLORE;
        }
    }

    @Override
    public void newDataReceived(DataStore data) {
        super.newDataReceived(data);

        if (data.get("type") == EntityType.AGENT_EXPLORER) {
            // Decide on updater role for each agent
            String myName = this.getLocalName();
            String otherName = ((String)data.get("updater_name"));
            if (myName.compareTo(otherName) < 0) {
                this.dataStore.put("updater_name", myName);
            } else {
                this.dataStore.put("updater_name", otherName);
            }
//            this.log("Comparing names: " + myName + " <> " + otherName + " ==> "
//                    + myName.compareTo(otherName));
        }
    }

    @Override
    public String getTypeSpecificInfo() {
        return "|map_s="+this.map.size()
                +"|opn="+this.getOpenedNodes().size()
                +"|t_p="+this.dataStore.get("tanker_position")
                +"|dest="+this.destination
                +"|u_n="+this.dataStore.get("updater_name")
                +"|ac="+this.dataStore.get("avoiding_conflict");
    }
}
