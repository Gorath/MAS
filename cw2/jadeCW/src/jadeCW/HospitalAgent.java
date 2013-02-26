package jadeCW;

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
public class HospitalAgent extends Agent {

    protected void setup() {

        // Read number of appointments from std::in
        int numOfAppointments = Integer.parseInt(System.console().readLine());

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

        // Make this agent terminate
        doDelete();
    }


}
