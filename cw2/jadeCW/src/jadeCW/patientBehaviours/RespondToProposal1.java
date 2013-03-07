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
			System.out.println("Patient - RespondToProposal1: message received and is not null");

			ACLMessage replyMessage = response.createReply();
		
			int theirCurrentAppointment = Integer.parseInt(response.getUserDefinedParameter("currentAppointment"));
			int theirPreferredAppointment = Integer.parseInt(response.getUserDefinedParameter("preferredAppointment"));

			if (patientState.isCurrentlyProposing()){
				replyMessage.setPerformative(ACLMessage.REJECT_PROPOSAL);
			} else if (theirPreferredAppointment != patientState.getMyAppointment()){
				replyMessage.setPerformative(ACLMessage.REJECT_PROPOSAL);
			} else {					
				boolean isAtLeastAsPreferred = patientState.isAppointmentBetterThanCurrent(theirCurrentAppointment);
				if (isAtLeastAsPreferred){
					AID appointmentAllocator = patientState.getAppointmentAllocator();
					ACLMessage message = new ACLMessage(ACLMessage.INFORM);

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
					replyMessage.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				} else {
					replyMessage.setPerformative(ACLMessage.REJECT_PROPOSAL);					
				}						
			}
			myAgent.send(replyMessage);
		} else {
			System.out.println("Hospital - RespondToQuery: blocked.");
			block();
		}
	}

}
