package jadeCW;

import java.io.IOException;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RespondToProposal1 extends CyclicBehaviour {

	private PatientState patientState;
    private static String conversationID = "propose-swap";

	public RespondToProposal1(PatientState patientState) {
		this.patientState = patientState;
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
				MessageTemplate.MatchConversationId(conversationID));

		ACLMessage received = myAgent.receive(mt);

		if (received != null) {
			System.out.println("Patient - RespondToProposal1: message received and is not null");
			int proposerAppointment = Integer.parseInt(received.getContent());

			//is appointment is at least as preferred as the appointment to be given away.
			boolean isAtLeastAsPreferred = patientState.isAppointmentAtLeastAsPreferredAsMyAppointment(proposerAppointment);
			
			//otherwise, if the appointment to be given away (my appointment in this patient) is not owned by us, or it is more preferred to the
			// proposerAppointment, then reject.
			
			ACLMessage replyMessage = received.createReply();
			replyMessage.setPerformative(ACLMessage.INFORM);
			try {
				replyMessage.setContentObject(appointmentOwner);
			} catch (IOException e) {
				e.printStackTrace();
			}
			myAgent.send(replyMessage);
		} else {
			System.out.println("Hospital - RespondToQuery: blocked.");
			block();
		}
	}

}
