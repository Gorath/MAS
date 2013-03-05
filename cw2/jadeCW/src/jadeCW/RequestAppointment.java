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

    @Override
    public void action() {
        AID myAllocator = ((PatientAgent)myAgent).getAppointmentAllocator();
        if (myAllocator != null) return;
        if (((PatientAgent)myAgent).hasAppointment()) return;

        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(myAllocator);
        request.setConversationId(conversationID);

        myAgent.send(request);

        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
                MessageTemplate.MatchInReplyTo(request.getReplyWith()));

        ACLMessage response = myAgent.receive(mt);
        
        if (response.getPerformative() == ACLMessage.CONFIRM){
            int allocatedAppointment = Integer.parseInt(response.getUserDefinedParameter("allocatedAppointment"));
            ((PatientAgent)myAgent).setAppointment(allocatedAppointment);
        }
    }

    @Override
    public boolean done() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
