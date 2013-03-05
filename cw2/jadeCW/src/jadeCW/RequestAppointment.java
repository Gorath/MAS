package jadeCW;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Created with IntelliJ IDEA.
 * User: gdj08
 * Date: 27/02/13
 * Time: 11:44
 * To change this template use File | Settings | File Templates.
 */
public class RequestAppointment extends Behaviour {

    static String conversationID = "book-appointment";
    private int step = 0;
    private MessageTemplate mt;
    
    @Override
    public void action() {
    	
    	switch (step){
	    case 0:
	        AID myAllocator = ((PatientAgent)myAgent).getAppointmentAllocator();
	        if (myAllocator == null) return;
	        if (((PatientAgent)myAgent).hasAppointment()) return;
	
	        System.out.println(">>>>>>");
	        
	        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
	        request.addReceiver(myAllocator);
	        request.setConversationId(conversationID);
	        request.setSender(myAgent.getAID());
	        request.setReplyWith("book"+System.currentTimeMillis());
	        
	        myAgent.send(request);
	        
	        mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
	                MessageTemplate.MatchInReplyTo(request.getReplyWith()));
	        //MessageTemplate mt = MessageTemplate.MatchConversationId(conversationID);
	        step = 1;
	        break;
	    case 1:
	        ACLMessage response = myAgent.receive(mt);
	        if (response != null) {
	        	System.out.println("Patient: response received and not null");
	            if (response.getPerformative() == ACLMessage.CONFIRM){
	                int allocatedAppointment = Integer.parseInt(response.getUserDefinedParameter("allocatedAppointment"));
	                ((PatientAgent)myAgent).setAppointment(allocatedAppointment);
	                step = 2;
	            }
	            else{
	            	step = 0;
	            }
	        }
	        else {
	        	System.out.println("Patient " + myAgent.getLocalName() + " - Request Appointment blocked.");
		        block();
	        }
	        System.out.println("Patient " + myAgent.getLocalName() + " appointment is: " + ((PatientAgent)myAgent).hasAppointment());
	        break;
    	}
    }

    @Override
    public boolean done() {
        return step == 2;
    }

}
