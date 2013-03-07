package jadeCW.hospitalBehaviours;

import java.io.IOException;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jadeCW.HospitalState;

public class RespondToQuery extends CyclicBehaviour{

    private static String conversationID = "find-appointment-owner";
	private final HospitalState hospitalState;

	public RespondToQuery(HospitalState hospitalState) {
		this.hospitalState = hospitalState;
	}

	@Override
	public void action() {
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
        										 MessageTemplate.MatchConversationId(conversationID));
        //MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

        ACLMessage received = myAgent.receive(mt);
        
        if (received != null) {
        	System.out.println("Hospital - RespondToQuery: message received and is not null");
        	System.out.println("Content is: " + received.getContent());
            int appointmentQuery = Integer.parseInt(received.getContent());
            
            AID appointmentOwner = hospitalState.getAppointmentOwner(appointmentQuery);
            
            if (appointmentOwner == null){
            	appointmentOwner = myAgent.getAID();
            }
            
            ACLMessage replyMessage = received.createReply();
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
