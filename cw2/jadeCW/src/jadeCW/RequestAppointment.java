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
    private ActionStep step = ActionStep.MAKE_REQUEST;
    private MessageTemplate mt;
    
    @Override
    public void action() {
    	PatientAgent patientAgent = (PatientAgent) myAgent;
		switch (step){
		    case MAKE_REQUEST:
		        AID myAllocator = patientAgent.getAppointmentAllocator();
		        if (myAllocator == null) return;
		        if (patientAgent.hasAppointment()) return;
		
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
		        step = ActionStep.WAIT_FOR_REPLY;
		        break;
		    case WAIT_FOR_REPLY:
		        ACLMessage response = myAgent.receive(mt);
		        if (response != null) {
		        	System.out.println("Patient " + myAgent.getLocalName() + " - RequestAppointment: response received and not null");
		            if (response.getPerformative() == ACLMessage.CONFIRM){
		                int allocatedAppointment = Integer.parseInt(response.getUserDefinedParameter("allocatedAppointment"));
		                patientAgent.setAppointment(allocatedAppointment);
		                step = ActionStep.FINISH;
		            }
		            else{
		            	step = ActionStep.MAKE_REQUEST;
		            }
		        }
		        else {
		        	System.out.println("Patient " + myAgent.getLocalName() + " - RequestAppointment: blocked.");
			        block();
		        }
		        System.out.println("Patient " + myAgent.getLocalName() + " appointment is: " + patientAgent.hasAppointment());
		        break;
			default:
				break;
    	}
    }

    @Override
    public boolean done() {
        return step == ActionStep.FINISH;
    }

}
