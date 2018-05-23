package mas.agents;

import env.Couple;
import env.EntityType;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import mas.behaviours.CollectionBehaviour;
import mas.behaviours.MoveToTankerBehaviour;
import mas.strategies.CollectionStrategy;
import mas.strategies.MoveToTankerPosCollectorStrategy;
import utils.PointOfInterest;

public class CollectorAgent extends Agent {

    private String tankerName;
    private int timeSinceEmpty;
    private int maxCapacity;

    private static final int TIME_BEFORE_EMPTY = 30;

    protected void setup() {
        super.setup();

        tankerName = null;
        timeSinceEmpty = 0;
        maxCapacity = this.getBackPackFreeSpace();
        this.setTankerName();

        strategies.put(M_COLLECT, new CollectionStrategy(this));
        strategies.put(M_MOVE_TO_TANKER, new MoveToTankerPosCollectorStrategy(this));

        CollectionBehaviour collectionBehaviour = new CollectionBehaviour(this);
        collectionBehaviour.setDataStore(this.dataStore);
        MoveToTankerBehaviour moveToTankerBehaviour = new MoveToTankerBehaviour(this);
        moveToTankerBehaviour.setDataStore(this.dataStore);

        fsm.registerState(collectionBehaviour, "Collection");
        fsm.registerState(moveToTankerBehaviour, "MoveToTanker");
        fsm.registerTransition("Collection", "CheckVoiceMail", Agent.A_CHECK_VOICEMAIL);
        fsm.registerTransition("CheckVoiceMail", "Collection", Agent.M_COLLECT);
        fsm.registerTransition("MoveToTanker", "CheckVoiceMail", Agent.A_CHECK_VOICEMAIL);
        fsm.registerTransition("CheckVoiceMail", "MoveToTanker", Agent.M_MOVE_TO_TANKER);
    }

    private void setTankerName() {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(EntityType.AGENT_TANKER.toString());
        dfd.addServices(sd);
        DFAgentDescription[] results;

        while(this.tankerName == null) {
            try {
                results = DFService.search(this, dfd);
                if (results.length > 0) {
                    for (DFAgentDescription d : results) {
                        tankerName = d.getName().getLocalName();
                    }
                }
            } catch (FIPAException e) {
                e.printStackTrace();
                return;
            }
        }
        this.log("Tanker name = " + tankerName);
    }

    @Override
    public int getMovementBehaviour() {
        if ((boolean)this.dataStore.get("random_walk"))
            return M_RANDOM_WALK;

        if ((boolean)this.dataStore.get("walk_to_random"))
            return M_WALK_TO_RANDOM;

        if ((boolean) this.dataStore.get("avoiding_conflict"))
            return M_AVOID_CONFLICT;

        if (this.getBackPackFreeSpace() == 0) {
            return M_MOVE_TO_TANKER;
        }

        if (this.timeSinceEmpty >= TIME_BEFORE_EMPTY) {
            return M_MOVE_TO_TANKER;
        }

        if (this.findRelevantPoint() == null) {
            if (this.openedNodes.isEmpty()) {
//                this.dataStore.put("walk_to_random", true);
                this.dataStore.put("walk_to_random_max_steps", 1);
                return M_WALK_TO_RANDOM;
            } else {
                return M_EXPLORE;
            }
        } else {
            return M_COLLECT;
        }
    }

    public PointOfInterest findRelevantPoint() {
        int maxValue = -1;
        int bestValue = -1;
        int capacity = this.getBackPackFreeSpace();
        String treasureType = this.getMyTreasureType();
        PointOfInterest maxPoi = null;
        PointOfInterest bestPoi = null;
        for (PointOfInterest p : this.points) {
            for (Couple<String, Integer> attr : p.getAttributes()) {
                if (attr.getLeft().equals(treasureType)) {
                    if (maxValue < attr.getRight()) {
                        maxValue = attr.getRight();
                        maxPoi = p;
                    }
                    if (bestValue < attr.getRight() && attr.getRight() <= maxCapacity) {
                        bestValue = attr.getRight();
                        bestPoi = p;
                    }
                }

            }
        }
        if (bestPoi == null) return maxPoi;
        else {
            if (bestValue > capacity) {
                // force emptying to tank
                timeSinceEmpty += TIME_BEFORE_EMPTY;
            }
            return bestPoi;
        }
    }

    @Override
    public void move(String nextNode) {
        this.timeSinceEmpty++;
        if(this.emptyMyBackPack(tankerName)) {
            this.timeSinceEmpty = 0;
        }
        super.move(nextNode);
    }

    @Override
    public void updateMap() {
        this.pick();
        super.updateMap();
    }

    @Override
    public String getTypeSpecificInfo() {
        return "|map_s="+this.map.size()
                +"|opn="+this.openedNodes.size()
                +"|t_p="+this.dataStore.get("tanker_position")
                +"|dst="+this.destination;
    }
}
