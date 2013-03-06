package jadeCW;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ProposeSwap extends Behaviour{

    private static String conversationID = "propose-swap";
    private ActionStep step = ActionStep.MAKE_REQUEST;
    private MessageTemplate mt;
    
	@Override
	public void action() {
		PatientAgent patientAgent = (PatientAgent) myAgent;
		if (patientAgent.hasSwapOccurred()) step = ActionStep.FINISH;
		
		switch (step){
		    case MAKE_REQUEST:
		        if (!patientAgent.hasAppointment()) return;

		        AID currentMostPreferredAppointmentOwner = patientAgent.getCurrentMostPreferredAppointmentOwner();
		        
		        ACLMessage request = new ACLMessage(ACLMessage.PROPOSE);
		        request.addReceiver(currentMostPreferredAppointmentOwner);
		        request.setConversationId(conversationID);
		        request.setSender(patientAgent.getAID());
		        request.setReplyWith("propose-swap"+System.currentTimeMillis());
		        request.setContent(String.valueOf(patientAgent.getAppointment()));
		        
		        myAgent.send(request);
		        
		        mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
		                MessageTemplate.MatchInReplyTo(request.getReplyWith()));
		        //MessageTemplate mt = MessageTemplate.MatchConversationId(conversationID);
		        step = ActionStep.WAIT_FOR_REPLY;
		        break;
		    case WAIT_FOR_REPLY:
		        ACLMessage response = myAgent.receive(mt);
		        if (response != null) {
		        	System.out.println("Patient " + myAgent.getLocalName() + " - ProposeSwap: response received and not null");
		            if (response.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
		                patientAgent.setAppointment(patientAgent.getMostPreferredAppointment());
		                patientAgent.swapOccurred();
		                step = ActionStep.FINISH;
		            }
		            else if (response.getPerformative() == ACLMessage.REJECT_PROPOSAL){
		            	step = ActionStep.MAKE_REQUEST;
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
