package jadeCW.hospitalBehaviours;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jadeCW.HospitalState;

import java.io.IOException;
import java.util.HashMap;

public class UpdateAppointments extends CyclicBehaviour {

	private static String conversationID = "swapped-appointments";
	private final HospitalState hospitalState;
	private final HashMap<AID, AID> swaps;	
	
	public UpdateAppointments(HospitalState hospitalState) {
		this.hospitalState = hospitalState;
		this.swaps = new HashMap<AID, AID>();
	}

	@Override
	public void action() {
		MessageTemplate messageTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
				MessageTemplate.MatchConversationId(conversationID));

		ACLMessage response = myAgent.receive(messageTemplate);
				
		if (response != null) {
			System.out.println("Hospital - UpdateAppointments");

			try {			
				AID agentA = response.getSender();
				AID agentB = (AID) response.getContentObject();
				if (swaps.get(agentB) != null && swaps.get(agentB).equals(agentA)){					
					int previousAppointment = Integer.parseInt(response.getUserDefinedParameter("previousAppointment"));
					int newAppointment = Integer.parseInt(response.getUserDefinedParameter("newAppointment"));					
					hospitalState.setAppointment(newAppointment, agentA);
					hospitalState.setAppointment(previousAppointment, agentB);
					swaps.remove(agentB);
				} else {
					swaps.put(agentA, agentB);
				}
			} catch (UnreadableException e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("Hospital - RespondToQuery: blocked.");
			block();
		}
	}

}
