package jadeCW.hospitalBehaviours;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jadeCW.HospitalState;

import java.util.HashMap;

public class UpdateAppointments extends CyclicBehaviour {

	private static final long serialVersionUID = 1L;
	
	private static final String conversationID = "swapped-appointments";
	private final HospitalState hospitalState;
	private final HashMap<AID, AID> swapsInformed;	
	
	public UpdateAppointments(HospitalState hospitalState) {
		this.hospitalState = hospitalState;
		this.swapsInformed = new HashMap<AID, AID>();
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
				if (swapsInformed.get(agentB) != null && swapsInformed.get(agentB).equals(agentA)){					
					int currentAppointment = Integer.parseInt(response.getUserDefinedParameter("currentAppointment"));
					int newAppointment = Integer.parseInt(response.getUserDefinedParameter("newAppointment"));					
					hospitalState.setAppointment(newAppointment, agentA);
					hospitalState.setAppointment(currentAppointment, agentB);
					swapsInformed.remove(agentB);
				} else {
					swapsInformed.put(agentA, agentB);
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
