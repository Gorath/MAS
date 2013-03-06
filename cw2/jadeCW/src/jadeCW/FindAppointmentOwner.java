package jadeCW;

import java.util.ArrayList;
import java.util.List;

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
    private ActionStep step;
    private MessageTemplate mt;
    private List<Integer> preferredAppointments;
    private int mostPreferred;

    public FindAppointmentOwner() {
    	step = ActionStep.GET_PREFERRED_APPOINTMENTS;
    	preferredAppointments = new ArrayList<Integer>();
	}

    @Override
    public void action() {
        PatientAgent patientAgent = (PatientAgent) myAgent;
		switch(step) {
			case GET_PREFERRED_APPOINTMENTS:
				preferredAppointments = patientAgent.getMorePreferredAppointments();
				step = ActionStep.MAKE_REQUEST;
				break;
            case MAKE_REQUEST:
                AID allocator = patientAgent.getAppointmentAllocator();
                if (!patientAgent.hasAppointment()) return;

                // If we have no preferred appointments left to try then stop trying
                if (preferredAppointments.size() == 0 ) {
                    step = ActionStep.FINISH;
                    return;
                }
                
                mostPreferred = preferredAppointments.remove(0);
                patientAgent.setMostPreferredAppointment(mostPreferred);
                

                // If we have a preferred appointment send re-arrange request
                // request to get the patient which has the appointment that we want
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);

                request.addReceiver(allocator);
                request.setConversationId(conversationID);
                request.setSender(myAgent.getAID());
                request.setReplyWith("find-owner"+System.currentTimeMillis());
                request.setContent(String.valueOf(mostPreferred));

                myAgent.send(request);

                mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
                                         MessageTemplate.MatchInReplyTo(request.getReplyWith()));
                step = ActionStep.WAIT_FOR_REPLY;
                break;
            case WAIT_FOR_REPLY:
                ACLMessage response = myAgent.receive(mt);
                if (response != null) {
                    System.out.println("Patient " + myAgent.getLocalName() + " - FindAppointmentOwner: Behaviour received response which is not null");
                    if (response.getPerformative() == ACLMessage.INFORM){
                        try {
                            AID patientAID = (AID) response.getContentObject();
                            if(patientAID != null){
                            	patientAgent.setCurrentMostPreferredAppointmentOwner(patientAID);
                            	System.out.println("> Preferred appointment " + mostPreferred + " is owned by " + patientAID.getLocalName());
                            }
                            else{
                            	patientAgent.addAvailableAppointment(mostPreferred);
                            	System.out.println("appointment " + mostPreferred + " is free");
                            }
                        } catch (UnreadableException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        step = ActionStep.FINISH;
                    }
                    else{
                        step = ActionStep.MAKE_REQUEST;
                    }
                }
                else {
                    System.out.println("Patient " + myAgent.getLocalName() + " - FindAppointmentOwner: blocked.");
                    block();
                }
                break;
            default:
            	break;
        }
    }

    @Override
    public boolean done() {
        return step == ActionStep.FINISH;
    }
}
