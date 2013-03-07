package jadeCW.patientBehaviours;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jadeCW.ActionStep;
import jadeCW.PatientState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gdj08
 * Date: 05/03/13
 * Time: 14:10
 * To change this template use File | Settings | File Templates.
 */
public class FindAppointmentOwner extends Behaviour {

	private static final long serialVersionUID = 1L;
	
	private final static String conversationID = "find-appointment-owner";
    private ActionStep step;
    private MessageTemplate messageTemplate;
    private List<Integer> preferredAppointments;
	private PatientState patientState;
	private int mostPreferred;

    public FindAppointmentOwner(PatientState patientState) {
    	this.patientState = patientState;
		step = ActionStep.INIT;
    	preferredAppointments = new ArrayList<Integer>();
    }

	@Override
    public void action() {
		switch(step) {
			case INIT:
				//System.out.println(">>>>>>>>>>>>>>bugaloo!");
				if (patientState.hasAppointment()) {
					//System.out.println(">> Patient - FindAppointmentOwner: INITIALISATION");
					preferredAppointments = patientState.getMorePreferredAppointments();
					step = ActionStep.MAKE_REQUEST;
				}
				break;
            case MAKE_REQUEST:
                AID appointmentAllocator = patientState.getAppointmentAllocator();

                // If we have no preferred appointments left to try then stop trying
                if (preferredAppointments.size() == 0 ) {
                    step = ActionStep.FINISH;
                    return;
                }
                
                mostPreferred = preferredAppointments.remove(0);

                // If we have a preferred appointment send re-arrange request
                // request to get the patient which has the appointment that we want
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);

                request.addReceiver(appointmentAllocator);
                request.setConversationId(conversationID);
                request.setSender(myAgent.getAID());
                request.setReplyWith("find-owner"+System.currentTimeMillis());
                request.setContent(String.valueOf(mostPreferred));

                myAgent.send(request);
                
                System.out.println(">> " + myAgent.getLocalName() + " has requested the owner for current most preferred appointment " + mostPreferred);
                
                messageTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
                                         MessageTemplate.MatchInReplyTo(request.getReplyWith()));
                step = ActionStep.WAIT_FOR_REPLY;
                break;
            case WAIT_FOR_REPLY:
                ACLMessage response = myAgent.receive(messageTemplate);
                if (response != null) {
                    if (response.getPerformative() == ACLMessage.INFORM){
                        try {
                        	// We have been told the appointment owner and so we set it as a 
                        	// mapping from appointment (int) -> owner (AID) in patient state
                            AID patientAID = (AID) response.getContentObject();
                            patientState.addAppointmentOwner(mostPreferred, patientAID);
                            System.out.println("<<<< " + myAgent.getLocalName() + " received message, owner of appointment " + mostPreferred + " is " + patientAID.getLocalName());
                        } catch (Exception e) {
                            e.printStackTrace(); 
                        }
                    }
                    // go back to MAKE_REQUEST step to find the appointment owner for the next most preferred appointment
                    step = ActionStep.MAKE_REQUEST;
                }
                else {
                    //System.out.println("Patient " + myAgent.getLocalName() + " - FindAppointmentOwner: blocked.");
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
