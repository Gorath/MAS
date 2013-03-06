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
    private MessageTemplate messageTemplate;
    private List<Integer> preferredAppointments;
	private PatientState patientState;

    public FindAppointmentOwner(PatientState patientState) {
    	this.patientState = patientState;
		step = ActionStep.INIT;
    	preferredAppointments = new ArrayList<Integer>();
    }

	@Override
    public void action() {
		switch(step) {
			case INIT:
				if (patientState.hasAppointment()) {
					preferredAppointments = patientState.getMorePreferredAppointments();
					step = ActionStep.MAKE_REQUEST;
				}
				break;
            case MAKE_REQUEST:
                AID appointmentAllocator = patientState.getAppointmentAllocator();

                // If we have no preferred appointments left to try then stop trying
                if (preferredAppointments.size() == 0 ) {
                    step = ActionStep.FINISH;
                    patientState.swapOccurred();
                    return;
                }
                
                int mostPreferred = preferredAppointments.remove(0);
                patientState.setMostPreferredAppointment(mostPreferred);

                // If we have a preferred appointment send re-arrange request
                // request to get the patient which has the appointment that we want
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);

                request.addReceiver(appointmentAllocator);
                request.setConversationId(conversationID);
                request.setSender(myAgent.getAID());
                request.setReplyWith("find-owner"+System.currentTimeMillis());
                request.setContent(String.valueOf(mostPreferred));

                myAgent.send(request);

                messageTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
                                         MessageTemplate.MatchInReplyTo(request.getReplyWith()));
                step = ActionStep.WAIT_FOR_REPLY;
                break;
            case WAIT_FOR_REPLY:
                ACLMessage response = myAgent.receive(messageTemplate);
                if (response != null) {
                    System.out.println("Patient " + myAgent.getLocalName() + " - FindAppointmentOwner: Behaviour received response which is not null");
                    if (response.getPerformative() == ACLMessage.INFORM){
                        try {
                            AID patientAID = (AID) response.getContentObject();
                            patientState.setCurrentMostPreferredAppointmentOwner(patientAID);
                            System.out.println("> Preferred appointment " + patientState.getMostPreferredAppointment() + " is owned by " + patientAID.getLocalName());
                        } catch (Exception e) {
                            e.printStackTrace(); 
                        }
                        step = ActionStep.WAIT_FOR_PROPOSE_BEHAVIOUR;
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
            case WAIT_FOR_PROPOSE_BEHAVIOUR:
            	if (patientState.hasProposalBeenRejected()){
            		patientState.setProposalRejection(false);
            		step = ActionStep.MAKE_REQUEST;
            		return;
            	} 
            	if (patientState.hasSwapOccurred()) {
            		step = ActionStep.FINISH;
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
