package jadeCW.hospitalBehaviours;

import java.io.IOException;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jadeCW.HospitalState;

public class RespondToProposal2 extends CyclicBehaviour {

	private static String conversationID = "propose-swap";
	private final HospitalState hospitalState;

	public RespondToProposal2(HospitalState hospitalState) {
		this.hospitalState = hospitalState;
	}

	@Override
	public void action() {
		MessageTemplate messageTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
				MessageTemplate.MatchConversationId(conversationID));

		ACLMessage response = myAgent.receive(messageTemplate);

		if (response != null) {
			System.out.println("Hospital - RespondToProposal1: message received and is not null");

			ACLMessage replyMessage = response.createReply();

			int senderAppointment = Integer.parseInt(response.getUserDefinedParameter("senderAppointment"));
			int receiverAppointment = Integer.parseInt(response.getUserDefinedParameter("receiverAppointment"));

			boolean appointmentFree = hospitalState.isAppointmentFree(receiverAppointment-1);
			
			if (appointmentFree){
				hospitalState.setAppointment(receiverAppointment-1, response.getSender());
				hospitalState.setAppointment(senderAppointment-1, null);				
				replyMessage.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			} else {
				replyMessage.setPerformative(ACLMessage.REJECT_PROPOSAL);
				try {
					replyMessage.setContentObject(hospitalState.getAppointmentOwner(receiverAppointment));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			myAgent.send(replyMessage);
		} else {
			System.out.println("Hospital - RespondToQuery: blocked.");
			block();
		}
	}

}
