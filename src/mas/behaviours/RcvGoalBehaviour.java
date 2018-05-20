package mas.behaviours;

import env.EntityType;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import mas.agents.Agent;

import java.util.Date;
import java.util.List;

public class RcvGoalBehaviour extends ABehaviour {

    public RcvGoalBehaviour(Agent agent) {
        super(agent);
    }

    private List<String> findEscapePlan(String position, String otherPosition, List<String> planToAvoid) {
        String escapeNode = null;

        if (planToAvoid != null) {
            planLoop:
            for (int i = planToAvoid.size() - 1; i >= 0; i--) {
                String nodeInPlan = planToAvoid.get(i);
                for (String neighbor : this.agent.getMap().get(nodeInPlan)) {
                    if (!planToAvoid.contains(neighbor) && !neighbor.equals(otherPosition)) {
                        escapeNode = neighbor;
                        break planLoop;
                    }
                }
            }
        }

        return this.agent.computePlanTo(position, escapeNode);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
        ACLMessage msg = this.agent.blockingReceive(mt, Agent.WAIT_TIME);

        if (msg != null) {
            this.agent.log("Goal received, processing...");
            try {
                DataStore ds = (DataStore) msg.getContentObject();
                if (this.agent.getType() != EntityType.AGENT_TANKER) {
                    // update tanker_position if needed.
                    if (((Date)ds.get("tanker_position_date")).compareTo(
                            (Date)this.agent.getDataStore().get("tanker_position_date")) > 0) {

                        this.getDataStore().put("tanker_position", ds.get("tanker_position"));
                        this.getDataStore().put("tanker_position_date", ds.get("tanker_position_date"));
                    }
                }

                List<String> otherPlan = (List<String>) ds.get("current_plan");
                List<String> myPlan = this.agent.getCurrentPlan();

                boolean conflictForOther = false;
                boolean conflictForMe = false;

                if (otherPlan != null)
                    conflictForOther = agent.getCurrentPosition().equals(
                            otherPlan.get(otherPlan.size()-1));
                if (myPlan != null)
                    conflictForMe = ds.get("position").equals(myPlan.get(myPlan.size()-1));

                if ((conflictForMe && conflictForOther) || (conflictForOther && this.agent.getType() == EntityType.AGENT_TANKER)) {
                    this.agent.log("/!\\ CONFLICT /!\\ for both agents");

                    if ((boolean) this.getDataStore().get("avoiding_conflict") || (boolean)ds.get("avoiding_conflict")) {
                        this.agent.log("Already in conflict. Random walk");
                        this.getDataStore().put("random_walk", true);
                        this.getDataStore().put("random_walk_max_steps", 5);
                    }

                    List<String> myEscapePlan = findEscapePlan(this.agent.getCurrentPosition(), (String)ds.get("position"), otherPlan);
                    List<String> otherEscapePlan = findEscapePlan((String)ds.get("position"), this.agent.getCurrentPosition(), myPlan);

                    this.agent.log(""+myEscapePlan);
                    this.agent.log(""+otherEscapePlan);

                    if (myEscapePlan == null && otherEscapePlan != null) {
                        this.agent.log("I have priority, no escape plan for me...");
                        this.agent.delayCheckVoiceMail(Agent.TIME_STEP);
                    } else if (myEscapePlan != null && otherEscapePlan == null) {
                        // Other has priority
                        this.agent.log("Other has priority, no escape plan for him...");
                        this.agent.setPlan(myEscapePlan);
                        this.getDataStore().put("avoiding_conflict", true);
                    }

                    if (myEscapePlan == null && otherEscapePlan == null) {
                        this.agent.log("No route to avoid conflict for both agents, going to random walk.");
                        this.getDataStore().put("random_walk", true);
                        this.getDataStore().put("random_walk_max_steps", 5);
                    }

                    if (myEscapePlan != null && otherEscapePlan != null) {

                        // Prior is the agent with the longest escape plan.
                        if (myEscapePlan.size() > otherEscapePlan.size()) {
                            this.agent.log("I have priority");
                            this.agent.delayCheckVoiceMail(Agent.TIME_STEP);
                        } else {
                            this.agent.log("Other has priority");
                            this.agent.setPlan(myEscapePlan);
                            this.getDataStore().put("avoiding_conflict", true);
                        }
                    }
                }

                if (conflictForMe && !conflictForOther) {
                    this.agent.delayCheckVoiceMail(Agent.TIME_STEP*2);
                }

            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            this.getDataStore().put("goal_exchanged", (int)this.getDataStore().get("goal_exchanged")+1);
//            this.getDataStore().put("goal_exchanged", ((int)this.getDataStore().get("goal_exchanged"))+1);
            // empty mail
            while(this.agent.receive(mt) != null);
        } else {
            this.agent.log("No goal received... ");
        }
    }

    @Override
    public boolean done() {
        return true;
    }

    public int onEnd() {
        return Agent.A_CHECK_VOICEMAIL;
    }
}
