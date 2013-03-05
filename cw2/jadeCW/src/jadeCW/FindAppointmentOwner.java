package jadeCW;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * Created with IntelliJ IDEA.
 * User: gdj08
 * Date: 05/03/13
 * Time: 14:10
 * To change this template use File | Settings | File Templates.
 */
public class FindAppointmentOwner extends Behaviour {

    private static String conversationID = "find-appointment-owner";
    private int step = 0;
    private MessageTemplate mt;

    //private List<Integer> alreadyTried = new ArrayList<Integer>;

    @Override
    public void action() {
        switch(step) {
            case 0:
                AID allocator = ((PatientAgent)myAgent).getAppointmentAllocator();
                if (!((PatientAgent)myAgent).hasAppointment()) return;
                int preferredAppointment = ((PatientAgent)myAgent).getMorePreferredAppointment();

                // If we have no preferred appointments left to try then stop trying
                if (preferredAppointment == -1 ) {
                    step = 2;
                    return;
                }

                // If we have a preferred appointment send re-arrange request
                // request to get the patient which has the appointment that we want
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);

                request.addReceiver(allocator);
                request.setConversationId(conversationID);
                request.setSender(myAgent.getAID());
                request.setReplyWith("find-owner"+System.currentTimeMillis());
                request.setContent(String.valueOf(preferredAppointment));

                myAgent.send(request);

                mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
                                         MessageTemplate.MatchInReplyTo(request.getReplyWith()));
                step = 1;
                break;
            case 1:
                ACLMessage response = myAgent.receive(mt);
                if (response != null) {
                    System.out.println("FindAppointOwner Behaviour received response which is not null");
                    if (response.getPerformative() == ACLMessage.INFORM){
                        try {
                            AID patientAID = (AID) response.getContentObject();
                            //then do something with the aid of the patient, i.e. negotiate with that patient
                        } catch (UnreadableException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        step = 2;
                    }
                    else{
                        step = 0;
                    }
                }
                else {
                    System.out.println("FindAppointOwner " + myAgent.getLocalName() + " - blocked.");
                    block();
                }
                break;
        }
    }

    @Override
    public boolean done() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
