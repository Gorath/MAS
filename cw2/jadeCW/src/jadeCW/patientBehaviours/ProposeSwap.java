package jadeCW.patientBehaviours;

import java.io.IOException;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jadeCW.ActionStep;
import jadeCW.PatientState;

public class ProposeSwap extends Behaviour{

    private final static String proposeConversationID = "propose-swap";
    private final static String informSwapConversationID = "swapped-appointments";
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
				} else if (patientState.isCurrentlyProposing()){
					step = ActionStep.MAKE_REQUEST;
				}
				break;
		    case MAKE_REQUEST:{
		        AID currentMostPreferredAppointmentOwner = patientState.getCurrentMostPreferredAppointmentOwner();
		        
		        ACLMessage request = new ACLMessage(ACLMessage.PROPOSE);
		        request.addReceiver(currentMostPreferredAppointmentOwner);
		        request.setConversationId(proposeConversationID);
		        request.setSender(myAgent.getAID());
		        request.setReplyWith("propose-swap"+System.currentTimeMillis());		        		        
		        request.addUserDefinedParameter("senderAppointment", String.valueOf(patientState.getMyAppointment()));
		        request.addUserDefinedParameter("receiverAppointment", String.valueOf(patientState.getCurrentMostPreferredAppointment()));
		        
		        myAgent.send(request);
		        
		        messageTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(proposeConversationID),
		                MessageTemplate.MatchInReplyTo(request.getReplyWith()));
		        
		        step = ActionStep.WAIT_FOR_REPLY;
		        
		        break;
		    }
		    case WAIT_FOR_REPLY:{
		        ACLMessage response = myAgent.receive(messageTemplate);
		        if (response != null) {
		        	System.out.println("Patient " + myAgent.getLocalName() + " - ProposeSwap: response received and not null");
		            if (response.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
	                	AID appointmentAllocator = patientState.getAppointmentAllocator();
		            	AID currentMostPreferredAppointmentOwner = patientState.getCurrentMostPreferredAppointmentOwner();
		            	if (!appointmentAllocator.equals(currentMostPreferredAppointmentOwner)){		            	
		            		ACLMessage inform = new ACLMessage(ACLMessage.INFORM);

		                	inform.addReceiver(appointmentAllocator);
		                	inform.setConversationId(informSwapConversationID);
		                	inform.setSender(myAgent.getAID());
		                	inform.addUserDefinedParameter("previousAppointment", String.valueOf(patientState.getMyAppointment()));
		                	inform.addUserDefinedParameter("newAppointment", String.valueOf(patientState.getCurrentMostPreferredAppointment()));
		                    try {
								inform.setContentObject(currentMostPreferredAppointmentOwner);
							} catch (IOException e) {
								e.printStackTrace();
							}

		                	myAgent.send(inform);		            	
		            	}
		                patientState.setAppointment(patientState.getCurrentMostPreferredAppointment());
		                patientState.swapOccurred();
		                step = ActionStep.FINISH;
		            }
		            else if (response.getPerformative() == ACLMessage.REJECT_PROPOSAL){
		            	step = ActionStep.INIT;
		            }
	            	patientState.setCurrentlyProposing(false);
		        }
		        else {
		        	System.out.println("Patient " + myAgent.getLocalName() + " - ProposeSwap: blocked.");
			        block();
		        }
		        break;
		        }
			default:
				break;
		}
	}

	@Override
	public boolean done() {
		return step == ActionStep.FINISH;
	}

}
