package jadeCW.patientBehaviours;

import java.io.IOException;
import java.util.List;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jadeCW.ActionStep;
import jadeCW.PatientState;

public class ProposeSwap extends Behaviour{

	private static final long serialVersionUID = 1L;
	
	private final static String proposeConversationID = "propose-swap";
    private final static String informSwapConversationID = "swapped-appointments";
    private ActionStep step;
    private MessageTemplate messageTemplate;
	private PatientState patientState;
    private List<Integer> preferredAppointments;

    
	public ProposeSwap(PatientState patientState) {
		this.patientState = patientState;
		step = ActionStep.INIT;
	}

	@Override
	public void action() {
		
		switch (step){
			case INIT:
				if (patientState.hasAppointment()) {
					preferredAppointments = patientState.getMorePreferredAppointments();
					step = ActionStep.MAKE_REQUEST;
				}
				break;
		    case MAKE_REQUEST:
		    	
                if (preferredAppointments.size() == 0 ) {
                    step = ActionStep.FINISH;
                    return;
                }
                
		    	int preferredAppointment = preferredAppointments.get(0);
		    	AID preferredAppointmentOwner = patientState.getAppointmentOwner(preferredAppointment);
		    	if (preferredAppointmentOwner != null){
		    		patientState.setCurrentlyProposing(true);
			        ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
			        messageTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(proposeConversationID),
			                MessageTemplate.MatchInReplyTo(message.getReplyWith()));
			        message.addReceiver(preferredAppointmentOwner);
			        message.setConversationId(proposeConversationID);
			        message.setSender(myAgent.getAID());
			        message.setReplyWith("propose-swap"+System.currentTimeMillis());		        		        
			        message.addUserDefinedParameter("currentAppointment", String.valueOf(patientState.getMyAppointment()));
			        message.addUserDefinedParameter("preferredAppointment", String.valueOf(preferredAppointment));
			        myAgent.send(message);	
			        step = ActionStep.WAIT_FOR_REPLY;
		    	}
		        break;
		    
		    case WAIT_FOR_REPLY:
		        ACLMessage response = myAgent.receive(messageTemplate);
		        if (response != null) {
                	int newAppointment = preferredAppointments.remove(0);

		        	System.out.println("Patient " + myAgent.getLocalName() + " - ProposeSwap: response received and not null");
		            if (response.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
	                	AID appointmentAllocator = patientState.getAppointmentAllocator();
		            	AID swappedAppointmentOwner = response.getSender();
		            	if (!appointmentAllocator.equals(swappedAppointmentOwner)){
	                    	ACLMessage message = new ACLMessage(ACLMessage.INFORM);

		                    try {
			                	message.addReceiver(appointmentAllocator);
			                	message.setConversationId(informSwapConversationID);
			                	message.setSender(myAgent.getAID());
			                	message.addUserDefinedParameter("currentAppointment", String.valueOf(patientState.getMyAppointment()));
			                	message.addUserDefinedParameter("newAppointment", String.valueOf(newAppointment));
								message.setContentObject(swappedAppointmentOwner);
			                	myAgent.send(message);		            	
							} catch (IOException e) {
								e.printStackTrace();
							}
		            	}
		                patientState.setAppointment(newAppointment);
		                step = ActionStep.FINISH;
		            } else if (response.getPerformative() == ACLMessage.REJECT_PROPOSAL){
		            	step = ActionStep.MAKE_REQUEST;
		            }
		            patientState.removeKnownAppointmentOwner(newAppointment);
		    		patientState.setCurrentlyProposing(false);
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
