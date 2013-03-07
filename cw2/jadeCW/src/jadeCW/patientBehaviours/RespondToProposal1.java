package jadeCW.patientBehaviours;

import java.io.IOException;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jadeCW.PatientState;

public class RespondToProposal1 extends CyclicBehaviour {

	private PatientState patientState;
	private static String conversationID = "propose-swap";
	private static String informSwapConversationID = "swapped-appointments";

	public RespondToProposal1(PatientState patientState) {
		this.patientState = patientState;
	}

	@Override
	public void action() {
		if (patientState.isCurrentlyProposing()){
			return;
		}
		
		MessageTemplate messageTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
				MessageTemplate.MatchConversationId(conversationID));

		ACLMessage response = myAgent.receive(messageTemplate);

		if (response != null) {
			System.out.println("Patient - RespondToProposal1: message received and is not null");

			ACLMessage replyMessage = response.createReply();

			int senderAppointment = Integer.parseInt(response.getUserDefinedParameter("senderAppointment"));
			int receiverAppointment = Integer.parseInt(response.getUserDefinedParameter("receiverAppointment"));

			if (receiverAppointment != patientState.getMyAppointment()){
				replyMessage.setPerformative(ACLMessage.REJECT_PROPOSAL);
			} else {					
				boolean isAtLeastAsPreferred = patientState.isAppointmentBetterThanCurrent(senderAppointment);
				if (isAtLeastAsPreferred){
					AID appointmentAllocator = patientState.getAppointmentAllocator();
					ACLMessage inform = new ACLMessage(ACLMessage.INFORM);

					inform.addReceiver(appointmentAllocator);
					inform.setConversationId(informSwapConversationID);
					inform.setSender(myAgent.getAID());
                	inform.addUserDefinedParameter("previousAppointment", String.valueOf(receiverAppointment));
                	inform.addUserDefinedParameter("newAppointment", String.valueOf(senderAppointment));
					try {
						inform.setContentObject(response.getSender());
					} catch (IOException e) {
						e.printStackTrace();
					}

					myAgent.send(inform);		            						

					patientState.setAppointment(senderAppointment);
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
