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
			ACLMessage replyMessage = response.createReply();

			int theirCurrentAppointment = Integer.parseInt(response.getUserDefinedParameter("currentAppointment"));
			int theirPreferredAppointment = Integer.parseInt(response.getUserDefinedParameter("preferredAppointment"));

			boolean appointmentFree = hospitalState.isAppointmentFree(theirPreferredAppointment);
			
			System.out.println("Hospital - is the appointment " + theirPreferredAppointment + " free: " + appointmentFree);
			
			if (appointmentFree){
				hospitalState.setAppointment(theirPreferredAppointment, response.getSender());
				hospitalState.setAppointment(theirCurrentAppointment, null);				
				replyMessage.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			} else {
				replyMessage.setPerformative(ACLMessage.REJECT_PROPOSAL);
				replyMessage.addUserDefinedParameter("rejectionType", "3");
				try {
					replyMessage.setContentObject(hospitalState.getAppointmentOwner(theirPreferredAppointment));
					System.out.println("Hospital - rejected proposal, owner has changed to " + hospitalState.getAppointmentOwner(theirPreferredAppointment).getLocalName());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
			myAgent.send(replyMessage);
		} else {
			//System.out.println("Hospital - RespondToProposal2: blocked.");
			block();
		}
	}

}
