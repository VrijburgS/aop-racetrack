/**
 * Created by guro saria on 26-11-2015.
 */



import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;

import java.util.ArrayList;

public class Field extends Agent {
    //0 is free, 1 is occupied, 2 is off track
    private int[] map =  {2, 1, 0, 2,
            2, 1, 0, 2,
            2, 1, 2, 1};

    //oneday maybe we will use ondisk database, but for now:
    private ArrayList<AID> registeredAIDS = new ArrayList<AID>();
    //will hold positions etc
    private ArrayList<Competitor> competitors = new ArrayList<Competitor>();

    public final int mapHeight= 3;
    public final int mapWidth = 4;

    private Logger myLogger = Logger.getMyLogger(getClass().getName());

    private class WaitPingAndReplyBehaviour extends CyclicBehaviour {

        public WaitPingAndReplyBehaviour(Agent a) {
            super(a);
        }

        public void action() {
            ACLMessage  msg = myAgent.receive();
            if(msg != null){
                myLogger.log(Logger.INFO, getName() + " got message from " + msg.getSender());
                ACLMessage reply = msg.createReply();
                if (msg.getPerformative()== ACLMessage.REQUEST) {
                    if (msg.getOntology().equals("space")) {
                        String content = msg.getContent();
                        if (content == null) {
                            reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                        } else {
                            if (!registeredAIDS.contains(msg.getSender())) {
                                reply.setContent("you are not registered");
                                reply.setOntology("space");
                                reply.setPerformative(ACLMessage.FAILURE);
                            } else if (content.contains(",")) {
                                //TODO: check if msg content is valid before stripping coordinates
                                reply.setPerformative(ACLMessage.INFORM);
                                int commaPos = content.indexOf(',');
                                String xStr = content.substring(0, commaPos);
                                String yStr = content.substring(commaPos + 1, content.length());
                                if (isInteger(xStr) && isInteger(xStr)) {
                                    int x = Integer.parseInt(xStr);
                                    int y = Integer.parseInt(yStr);
                                    int coInt = getCoordinate(x, y);
                                    switch (coInt) {
                                        case (0):
                                            reply.setContent("free");
                                            break;
                                        case (1):
                                            reply.setContent("occupied");
                                            break;
                                        case (2):
                                            reply.setContent("off track");
                                            break;
                                        case (-1):
                                            reply.setContent("out of bounds");
                                            break;
                                        default:
                                            reply.setContent("wtf");
                                            break;
                                    }

                                }

                            } else {
                                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                            }

                        }
                        send(reply);
                    }
                    if (msg.getOntology().equals("register")) {
                        reply.setOntology("register");
                        if (registeredAIDS.contains(msg.getSender())) {
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent(msg.getSender() + " is already registered, need unique ID");
                        } else {
                            reply.setPerformative(ACLMessage.CONFIRM);
                            registeredAIDS.add(msg.getSender());
                            Competitor c = new Competitor(msg.getSender());
                            competitors.add(c);
                        }
                        send(reply);
                    }
                }
                else {
                    block();
                }
            }
        }
    }


    protected void setup() {
        // Registration with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("FieldAgent");
        sd.setName(getName());
        sd.setOwnership("bassie");
        dfd.setName(getAID());
        dfd.addServices(sd);
        try {
            DFService.register(this,dfd);
            WaitPingAndReplyBehaviour PingBehaviour = new  WaitPingAndReplyBehaviour(this);
            addBehaviour(PingBehaviour);
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
            doDelete();
        }
    }
    //returns -1 if requested coordinate is out of bounds
    public int getCoordinate(int x, int y)
    {
        if (x > this.mapWidth-1 || y > this.mapHeight-1 || x < 0 || y < 0)
            return -1;
        return map[y * this.mapWidth + x];
    }
    //thank you Jonas Klemming
    private  static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     //test main
     public static void main(String [] args)
     {
     Field a = new Field();
     System.out.println( a.getCoordinate(0,1));
     }
     **/

}