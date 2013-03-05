package jadeCW;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.Property;

/**
 * Created with IntelliJ IDEA.
 * User: gdj08
 * Date: 26/02/13
 * Time: 10:22
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class HospitalAgent extends Agent {

	private int numOfAppointments;
	private AID[] appointments;
	
    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.numOfAppointments = Integer.parseInt((String) args[0]);
        }

        appointments = new AID[numOfAppointments];

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
            sd.addProperties(new Property("numOfAppointments", String.valueOf(numOfAppointments)));
            dfd.addServices(sd);

            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new AllocateAppointment(appointments));
        addBehaviour(new RespondToQuery());
        
    }

    public AID getAppointmentOwner(int appointmentNumber) {
        return appointments[appointmentNumber-1];
    }
    
    public void takeDown(){
    	for(int i = 0; i < numOfAppointments; i++){
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
