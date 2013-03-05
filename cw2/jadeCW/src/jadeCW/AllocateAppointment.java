package jadeCW;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class AllocateAppointment extends CyclicBehaviour{
	
	private static String conversationID = "book-appointment";
	private AID[] appointments;
	
	public AllocateAppointment(AID[] appointments) {
		this.appointments = appointments;
	}
	
	@Override
	public void action() {
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				 								 MessageTemplate.MatchConversationId(conversationID));

        ACLMessage received = myAgent.receive(mt);
        
        if (received != null) {
        	System.out.println("Hospital - AllocateAppointment: message received and is not null");
        	AID patient = received.getSender();
        	
            ACLMessage replyMessage = received.createReply();
            
            int i = getNextAvailableAppointment();
            if(i != -1){
            	appointments[i] = patient;
                replyMessage.setPerformative(ACLMessage.CONFIRM);
                replyMessage.addUserDefinedParameter("allocatedAppointment", String.valueOf(i+1));
            }
            else{
            	replyMessage.setPerformative(ACLMessage.REFUSE);
            }
            
            //replyMessage.addReceiver(patient);
            //replyMessage.setConversationId(conversationID);

            myAgent.send(replyMessage);
        }
        else {
        	System.out.println("Hospital - AllocateAppoinment blocked.");
        	block();
        }
	}

	private int getNextAvailableAppointment() {
        for (int i = 0; i < appointments.length; i++){
        	if (appointments[i] == null){
        		return i;
        	}
        }
        return -1;
	}

}
