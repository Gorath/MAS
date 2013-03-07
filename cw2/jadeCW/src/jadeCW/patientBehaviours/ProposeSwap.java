package jadeCW.patientBehaviours;

import java.io.IOException;
import java.util.List;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jadeCW.ActionStep;
import jadeCW.PatientState;

public class ProposeSwap extends Behaviour{

	private static final long serialVersionUID = 1L;
	
	private final static String proposeConversationID = "propose-swap";
    private final static String informSwapConversationID = "swapped-appointments";
    private final static int MAX_REQUESTS = 20;
    private ActionStep step;
    private MessageTemplate messageTemplate;
	private PatientState patientState;
    private List<Integer> preferredAppointments;
    private int proposalCounter;
    
	public ProposeSwap(PatientState patientState) {
		this.patientState = patientState;
		step = ActionStep.INIT;
		proposalCounter = 0;
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
		    	
		    	if (preferredAppointment == patientState.getMyAppointment()) {
		    	    step = ActionStep.FINISH;
                    return;	
		    	}
		    	
		    	AID preferredAppointmentOwner = patientState.getAppointmentOwner(preferredAppointment);
		    	if (preferredAppointmentOwner != null){
		    		patientState.setCurrentlyProposing(true); //set the currentlyProposing flag
		    		//send a propose message to another patient to propose a swap of appointments
			        ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
			        message.addReceiver(preferredAppointmentOwner);
			        message.setConversationId(proposeConversationID);
			        message.setSender(myAgent.getAID());
			        message.setReplyWith("propose-swap"+System.currentTimeMillis());		        		        
			        message.addUserDefinedParameter("currentAppointment", String.valueOf(patientState.getMyAppointment()));
			        message.addUserDefinedParameter("preferredAppointment", String.valueOf(preferredAppointment));
			        myAgent.send(message);	

			        messageTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(proposeConversationID),
			        		MessageTemplate.MatchInReplyTo(message.getReplyWith()));
			        
			        System.out.println(myAgent.getLocalName() + " proposes a swap to " + preferredAppointmentOwner.getLocalName() + " for appointment " + preferredAppointment);
			        
			        step = ActionStep.WAIT_FOR_REPLY;
		    	}
		        break;
		    
		    case WAIT_FOR_REPLY:
		        ACLMessage response = myAgent.receive(messageTemplate);
		        if (response != null) {
                	int newAppointment = preferredAppointments.get(0);
                	//remove the appointment->owner mapping now as we may add it on later
                	
		            if (response.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
	                	AID appointmentAllocator = patientState.getAppointmentAllocator();
		            	AID swappedAppointmentOwner = response.getSender();
		            	if (!appointmentAllocator.equals(swappedAppointmentOwner)){ 
		            		// if there is an appointment owner (i.e. not the hospital) then we can swap the appointments
		            		System.out.println(myAgent.getLocalName() + "'s proposal has been accepted and the proposed agent is not a hospital");
		            	
	                    	ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		                    try {
		                    	//send an inform message to the appointment allocator (Hospital Agent) to tell it
		                    	//that an appointment is to be swapped with another patient
			                	message.addReceiver(appointmentAllocator);
			                	message.setConversationId(informSwapConversationID);
			                	message.setSender(myAgent.getAID());
			                	message.addUserDefinedParameter("currentAppointment", String.valueOf(patientState.getMyAppointment()));
			                	message.addUserDefinedParameter("newAppointment", String.valueOf(newAppointment));
								message.setContentObject(swappedAppointmentOwner);
			                	myAgent.send(message);		    
			                	
			                	System.out.println(myAgent.getLocalName() + " wants to swap own appointment" + patientState.getMyAppointment() + " with " + newAppointment);
							} catch (IOException e) {
								e.printStackTrace();
							}
		            	}
		            	// set the new appointment in the parent agent (our patient)
		                patientState.setAppointment(newAppointment);
		                System.out.println(myAgent.getLocalName() + "'s new appointment is: " + newAppointment);
		                patientState.removeKnownAppointmentOwner(newAppointment);
		                preferredAppointments.remove(0);
		                
		                step = ActionStep.FINISH;
		            } else if (response.getPerformative() == ACLMessage.REJECT_PROPOSAL){
		            	// if the proposal is rejected then go back to MAKE_REQUEST step?
		            	System.out.println(myAgent.getLocalName() + "'s proposal has been rejected by " + response.getSender().getLocalName());
		            	// what if the rejected proposal is by the hospital, i.e. another patient managed to get that appointment just after you requested for the owner
		            	int rejectionType = Integer.parseInt(response.getUserDefinedParameter("rejectionType"));
		            	
		            	if (rejectionType == 0){
		            		proposalCounter++;
		            		if (proposalCounter > MAX_REQUESTS){
		    	                patientState.removeKnownAppointmentOwner(newAppointment);
		    	                preferredAppointments.remove(0);
		            			proposalCounter = 0;
		            		}
		            	}
		            	else if (rejectionType == 1 || rejectionType == 2) {
			                patientState.removeKnownAppointmentOwner(newAppointment);
			                preferredAppointments.remove(0);
		            	}
		            	else if (rejectionType == 3){
		            		//if the sender is the hospital then the rejection is due to the fact that the owner of the appointment has changed
			            	try {
								AID newOwnerForAppointment = (AID) response.getContentObject();
								patientState.addAppointmentOwner(newAppointment, newOwnerForAppointment);
							} catch (UnreadableException e) {
								e.printStackTrace();
							}
		            	}
		            	step = ActionStep.MAKE_REQUEST;
		            }
		            //set the currently proposing flag as false when we have finished receiving a message
		    		patientState.setCurrentlyProposing(false); 
		        }
		        else {
		        	//System.out.println("Patient " + myAgent.getLocalName() + " - ProposeSwap: blocked.");
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
