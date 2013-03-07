package jadeCW.hospitalBehaviours;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jadeCW.HospitalState;

public class AllocateAppointment extends CyclicBehaviour{
	
	private static final long serialVersionUID = 1L;
	
	private final static String conversationID = "book-appointment";
	private final HospitalState hospitalState;
	
	public AllocateAppointment(HospitalState hospitalState) {
		this.hospitalState = hospitalState;
	}
	
	@Override
	public void action() {
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				 								 MessageTemplate.MatchConversationId(conversationID));

        ACLMessage received = myAgent.receive(mt);
        
        if (received != null) {
        	AID patient = received.getSender();
        	
            ACLMessage replyMessage = received.createReply();
            
            int appointment = hospitalState.setNextAvailableAppointment(patient);
            if (appointment != -1){
                replyMessage.setPerformative(ACLMessage.CONFIRM);
                replyMessage.addUserDefinedParameter("allocatedAppointment", String.valueOf(appointment+1));
                System.out.println("Hospital " + (appointment+1) + " allocated for " + patient.getLocalName());
            }
            else{
            	replyMessage.setPerformative(ACLMessage.REFUSE);
            }
            
            myAgent.send(replyMessage);
        }
        else {
        	//System.out.println("Hospital - AllocateAppoinment blocked.");
        	block();
        }
	}
	
}
