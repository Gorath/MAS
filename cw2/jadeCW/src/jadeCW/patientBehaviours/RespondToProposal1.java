package jadeCW.patientBehaviours;

import java.io.IOException;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jadeCW.PatientState;

public class RespondToProposal1 extends CyclicBehaviour {

	private static final long serialVersionUID = 1L;

	private PatientState patientState;
	private static String proposeConversationID = "propose-swap";
	private static String informSwapConversationID = "swapped-appointments";

	public RespondToProposal1(PatientState patientState) {
		this.patientState = patientState;
	}

	@Override
	public void action() {		
		MessageTemplate messageTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
				MessageTemplate.MatchConversationId(proposeConversationID));

		ACLMessage response = myAgent.receive(messageTemplate);

		if (response != null) {
			ACLMessage replyMessage = response.createReply();
		
			int theirCurrentAppointment = Integer.parseInt(response.getUserDefinedParameter("currentAppointment"));
			int theirPreferredAppointment = Integer.parseInt(response.getUserDefinedParameter("preferredAppointment"));

			if (patientState.isCurrentlyProposing()){ 
				//if ProposeSwap is currently in the midst of proposing
				System.out.println(myAgent.getLocalName() + " is in the midst of proposing to another agent, " + response.getSender().getLocalName() + "'s proposal is rejected!");
				replyMessage.setPerformative(ACLMessage.REJECT_PROPOSAL);
				replyMessage.addUserDefinedParameter("rejectionType", "0");
			} else if (theirPreferredAppointment != patientState.getMyAppointment()){
				//if the proposed appointment is not the same as my appointment then reject
				// this would occur if the proposed agent / proposee (this agent) has swapped its appointment with another patient agent
				// in between the proposing agent's request to the hospital for the owner of this agent's previous appointment
				// and the request from the proposing agent to this agent for an appointment swap
				System.out.println(myAgent.getLocalName() + " is no longer the owner of appointment " + theirCurrentAppointment + " but owns  appointment " + patientState.getMyAppointment());
				replyMessage.setPerformative(ACLMessage.REJECT_PROPOSAL);
				replyMessage.addUserDefinedParameter("rejectionType", "1");
			} else {					
				boolean isAtLeastAsPreferred = patientState.isAppointmentBetterThanCurrent(theirCurrentAppointment);
				if (isAtLeastAsPreferred){
					//if the proposed appointment is better/more preferred than our current appointment, then accept the proposal
					
					AID appointmentAllocator = patientState.getAppointmentAllocator();
					ACLMessage message = new ACLMessage(ACLMessage.INFORM);
					//send message to the appointment allocator (hospital) to confirm that the appointments are swapped
					System.out.println(myAgent.getLocalName() + " accepts " + response.getSender().getLocalName() + "'s proposal!");
					try {
						message.addReceiver(appointmentAllocator);
						message.setConversationId(informSwapConversationID);
						message.setSender(myAgent.getAID());
	                	message.addUserDefinedParameter("currentAppointment", String.valueOf(theirPreferredAppointment));
	                	message.addUserDefinedParameter("newAppointment", String.valueOf(theirCurrentAppointment));
						message.setContentObject(response.getSender());
						myAgent.send(message);		            						
					} catch (IOException e) {
						e.printStackTrace();
					}

					patientState.setAppointment(theirCurrentAppointment);
	                System.out.println(myAgent.getLocalName() + "'s new appointment is: " + theirCurrentAppointment);
					replyMessage.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				} else {
					replyMessage.setPerformative(ACLMessage.REJECT_PROPOSAL);		
					replyMessage.addUserDefinedParameter("rejectionType", "2");
				}						
			}
			myAgent.send(replyMessage);
		} else {
			//System.out.println("Hospital - RespondToProposal1: blocked.");
			block();
		}
	}

}
