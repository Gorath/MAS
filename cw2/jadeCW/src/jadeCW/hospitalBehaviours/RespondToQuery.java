package jadeCW.hospitalBehaviours;

import java.io.IOException;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jadeCW.HospitalState;

public class RespondToQuery extends CyclicBehaviour{

	private static final long serialVersionUID = 1L;
	
	private static final String conversationID = "find-appointment-owner";
	private final HospitalState hospitalState;

	public RespondToQuery(HospitalState hospitalState) {
		this.hospitalState = hospitalState;
	}

	@Override
	public void action() {
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
        										 MessageTemplate.MatchConversationId(conversationID));

        ACLMessage response = myAgent.receive(mt);
        
        if (response != null) {
        	System.out.println("Hospital - RespondToQuery: message received and is not null");
        	System.out.println("Content is: " + response.getContent());
            int appointmentQuery = Integer.parseInt(response.getContent());
            
            AID appointmentOwner = hospitalState.getAppointmentOwner(appointmentQuery);
            
            if (appointmentOwner == null){
            	appointmentOwner = myAgent.getAID();
            }
            
            ACLMessage replyMessage = response.createReply();
            replyMessage.setPerformative(ACLMessage.INFORM);
            try {
				replyMessage.setContentObject(appointmentOwner);
			} catch (IOException e) {
				e.printStackTrace();
			}
            myAgent.send(replyMessage);
        }
        else {
        	System.out.println("Hospital - RespondToQuery: blocked.");
        	block();
        }
	}
	
	

}
