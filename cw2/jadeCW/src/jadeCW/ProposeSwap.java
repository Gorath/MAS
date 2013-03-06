package jadeCW;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ProposeSwap extends Behaviour{

    private static String conversationID = "propose-swap";
    private ActionStep step = ActionStep.MAKE_REQUEST;
    private MessageTemplate messageTemplate;
	private PatientState patientState;
    
	public ProposeSwap(PatientState patientState) {
		this.patientState = patientState;
	}

	@Override
	public void action() {
		
		switch (step){
			case INIT:
				if (patientState.hasSwapOccurred()) {
					step = ActionStep.FINISH;
					return;
				}
				if (patientState.canStartProposing()){
	            	patientState.setStartProposing(false);
					step = ActionStep.MAKE_REQUEST;
				}
				break;
		    case MAKE_REQUEST:
		        AID currentMostPreferredAppointmentOwner = patientState.getCurrentMostPreferredAppointmentOwner();
		        
		        ACLMessage request = new ACLMessage(ACLMessage.PROPOSE);
		        request.addReceiver(currentMostPreferredAppointmentOwner);
		        request.setConversationId(conversationID);
		        request.setSender(myAgent.getAID());
		        request.setReplyWith("propose-swap"+System.currentTimeMillis());
		        request.setContent(String.valueOf(patientState.getMyAppointment()));
		        
		        myAgent.send(request);
		        
		        messageTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
		                MessageTemplate.MatchInReplyTo(request.getReplyWith()));
		        
		        step = ActionStep.WAIT_FOR_REPLY;
		        break;
		    case WAIT_FOR_REPLY:
		        ACLMessage response = myAgent.receive(messageTemplate);
		        if (response != null) {
		        	System.out.println("Patient " + myAgent.getLocalName() + " - ProposeSwap: response received and not null");
		            if (response.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
		                patientState.setAppointment(patientState.getMostPreferredAppointment());
		                patientState.swapOccurred();
		                step = ActionStep.FINISH;
		            }
		            else if (response.getPerformative() == ACLMessage.REJECT_PROPOSAL){
	            		patientState.setProposalRejection(true);
		            	step = ActionStep.INIT;
		            }
		        }
		        else {
		        	System.out.println("Patient " + myAgent.getLocalName() + " - ProposeSwap: blocked.");
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
