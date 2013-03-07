package jadeCW.hospitalBehaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jadeCW.HospitalState;

import java.io.IOException;

public class RespondToProposal2 extends CyclicBehaviour {

	private static final long serialVersionUID = 1L;

	private static final String conversationID = "propose-swap";
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
			System.out.println("Hospital - RespondToProposal2: message received and is not null");

			ACLMessage replyMessage = response.createReply();

			int theirCurrentAppointment = Integer.parseInt(response.getUserDefinedParameter("currentAppointment"));
			int theirPreferredAppointment = Integer.parseInt(response.getUserDefinedParameter("preferredAppointment"));

			boolean appointmentFree = hospitalState.isAppointmentFree(theirPreferredAppointment-1);
			
			if (appointmentFree){
				hospitalState.setAppointment(theirPreferredAppointment-1, response.getSender());
				hospitalState.setAppointment(theirCurrentAppointment-1, null);				
				replyMessage.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			} else {
				replyMessage.setPerformative(ACLMessage.REJECT_PROPOSAL);
				try {
					replyMessage.setContentObject(hospitalState.getAppointmentOwner(theirPreferredAppointment));
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
