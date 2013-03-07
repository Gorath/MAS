package jadeCW.patientBehaviours;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jadeCW.ActionStep;
import jadeCW.PatientState;

/**
 * Created with IntelliJ IDEA.
 * User: gdj08
 * Date: 27/02/13
 * Time: 11:44
 * To change this template use File | Settings | File Templates.
 */
public class RequestAppointment extends Behaviour {

    private final static String conversationID = "book-appointment";
    private ActionStep step;
    private MessageTemplate messageTemplate;
	private PatientState patientState;
    
    public RequestAppointment(PatientState patientState) {
		this.patientState = patientState;
		step = ActionStep.INIT;
	}

	@Override
    public void action() {
		switch (step){
			case INIT:
				if (patientState.getAppointmentAllocator() != null){
					step = ActionStep.MAKE_REQUEST;
				}	
				break;		
		    case MAKE_REQUEST:
		        AID myAllocator = patientState.getAppointmentAllocator();
		        
		        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);

		        messageTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
		                MessageTemplate.MatchInReplyTo(request.getReplyWith()));
		        request.addReceiver(myAllocator);
		        request.setConversationId(conversationID);
		        request.setSender(myAgent.getAID());
		        request.setReplyWith("book"+System.currentTimeMillis());
		        
		        myAgent.send(request);		        

		        step = ActionStep.WAIT_FOR_REPLY;
		        break;
		    case WAIT_FOR_REPLY:
		        ACLMessage response = myAgent.receive(messageTemplate);
		        if (response != null) {
		        	System.out.println("Patient " + myAgent.getLocalName() + " - RequestAppointment: response received and not null");
		            if (response.getPerformative() == ACLMessage.CONFIRM){
		                int allocatedAppointment = Integer.parseInt(response.getUserDefinedParameter("allocatedAppointment"));
		                patientState.setAppointment(allocatedAppointment);
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
		        System.out.println("Patient " + myAgent.getLocalName() + " appointment is: " + patientState.hasAppointment());
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
