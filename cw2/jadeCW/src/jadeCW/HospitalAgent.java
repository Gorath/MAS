package jadeCW;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.Property;
import jadeCW.hospitalBehaviours.AllocateAppointment;
import jadeCW.hospitalBehaviours.RespondToProposal2;
import jadeCW.hospitalBehaviours.RespondToQuery;
import jadeCW.hospitalBehaviours.UpdateAppointments;

/**
 * Created with IntelliJ IDEA.
 * User: gdj08
 * Date: 26/02/13
 * Time: 10:22
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class HospitalAgent extends Agent {
	
    private HospitalState hospitalState;

	protected void setup() {

        Object[] args = getArguments();
        int numberOfAppointments = 0;
        if (args != null && args.length > 0) {
        	numberOfAppointments = Integer.parseInt((String) args[0]);
        }

        // Reguster service allocate-appointments
        String serviceName = "allocate-appointments";

        // Register the service
        System.out.println("Agent "+getLocalName()+" registering service \""+serviceName+"\" of type \"allocate-appointments\"");
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName(serviceName);
            sd.setType("allocate-appointments");
            // Agents that want to use this service need to "know" the weather-forecast-ontology
            sd.addOntologies("allocate-appointments-ontology");
            // Agents that want to use this service need to "speak" the FIPA-SL language
            sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
            sd.addProperties(new Property("numOfAppointments", String.valueOf(numberOfAppointments)));
            dfd.addServices(sd);

            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        
        hospitalState = new HospitalState(numberOfAppointments);

        addBehaviour(new AllocateAppointment(hospitalState));
        addBehaviour(new RespondToQuery(hospitalState));
        addBehaviour(new RespondToProposal2(hospitalState));
        addBehaviour(new UpdateAppointments(hospitalState));        
    }    
    
    public void takeDown(){
    	AID[] appointments = hospitalState.getAppointments();
    	for(int i = 0; i < appointments.length; i++){
    		int appointmentID = i+1;
    		AID patientAgent = appointments[i];
    		String patient;
    		if (patientAgent == null) {
    			patient = "null";
    		} else{
    			patient = patientAgent.getLocalName();
    		}
    		System.out.println("hopsital1: " + "Appointment " + appointmentID + ": " + patient);
    	}
    }

}
