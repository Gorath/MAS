package jadeCW;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class AllocateAppointment extends CyclicBehaviour{
	
	static String conversationID = "book-appointment";
	AID[] appointments;
	
	public AllocateAppointment(AID[] appointments) {
		this.appointments = appointments;
	}
	
	@Override
	public void action() {
        MessageTemplate mt = MessageTemplate.MatchConversationId(conversationID);

        ACLMessage response = myAgent.receive(mt);
        AID patient = response.getSender();
        
        ACLMessage replyMessage;
        
        int i = getNextAvailableAppointment();
        if(i != -1){
        	appointments[i] = patient;
            replyMessage = new ACLMessage(ACLMessage.CONFIRM);
            replyMessage.addUserDefinedParameter("allocatedAppointment", String.valueOf(i+1));
        }
        else{
        	replyMessage = new ACLMessage(ACLMessage.REFUSE);
        }
        
        replyMessage.addReceiver(patient);
        replyMessage.setConversationId(conversationID);

        myAgent.send(replyMessage);
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
