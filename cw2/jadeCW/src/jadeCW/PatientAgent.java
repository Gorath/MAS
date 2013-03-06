package jadeCW;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gdj08
 * Date: 26/02/13
 * Time: 10:39
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class PatientAgent extends Agent {

	private PatientState patientState;

	public PatientAgent(){
		//availableAppointments = new ArrayList<Integer>();
	}

	protected void setup() {

		String inputString = "";

		Object[] args = getArguments();
		if (args != null && args.length > 0) {

			for (Object arg : args){  //THIS is a hack, need to make a change
				inputString += (String) arg + " ";
			}
			System.out.println("Input String: " + inputString);
		}

		List<List<Integer>> appointmentPreferences = parsePatientAppointmentPreferences(inputString);
		patientState = new PatientState(appointmentPreferences);
	
		subscribeToDF();

		addBehaviour(new RequestAppointment(patientState));
		addBehaviour(new FindAppointmentOwner(patientState));
		addBehaviour(new ProposeSwap(patientState));
		addBehaviour(new RespondToProposal1(patientState));
	}

	private void subscribeToDF() {
		// Build the description used as template for the subscription
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription templateSd = new ServiceDescription();
		templateSd.setType("allocate-appointments");
		template.addServices(templateSd);

		SearchConstraints sc = new SearchConstraints();
		// We want to receive 10 results at most
		sc.setMaxResults(new Long(10));

		addBehaviour(new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, sc)) {
			protected void handleInform(ACLMessage inform) {
				System.out.println("Agent "+getLocalName()+": Notification received from DF");
				try {
					DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
					if (results.length > 0) {
						for (int i = 0; i < results.length; ++i) {
							DFAgentDescription dfd = results[i];
							AID provider = dfd.getName();
							// The same agent may provide several services; we are only interested
							Iterator it = dfd.getAllServices();
							while (it.hasNext()) {
								ServiceDescription sd = (ServiceDescription) it.next();
								if (sd.getType().equals("allocate-appointments")) {
									System.out.println("Appointment allocator found");
									System.out.println("- Service \""+sd.getName()+"\" provided by agent "+provider.getName());
									// Stores the allocator provider agent for later use
									patientState.setAppointmentAllocator(provider);
								}
							}
						}
					}
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		} );
	}

	public List<List<Integer>> parsePatientAppointmentPreferences(String line) {
		List<List<Integer>> appointmentPreferences = new ArrayList<List<Integer>>();

		String[] splitString = line.split("-");

		// If line is "-" then add a blank array list for consistency
		if (splitString.length == 0) {
			appointmentPreferences.add(new ArrayList<Integer>());
		}

		// For each sub-section of preferences add a new array list
		for (int i = 0; i < splitString.length; i++) {
			String[] splitPrefRow = splitString[i].split(" ");
			ArrayList<Integer> row = new ArrayList<Integer>();

			// Add each number in this preference depth to the ArrayList
			for (int j = 0; j < splitPrefRow.length; j++) {
				if (!splitPrefRow[j].equals(" ") && !splitPrefRow[j].equals("")) {
					row.add(Integer.parseInt(splitPrefRow[j]));
				}
			}

			appointmentPreferences.add(row);
		}

		return appointmentPreferences;
	}

	public void takeDown(){
		if (patientState != null){
			int myAppointment = patientState.getMyAppointment();
			String appointmentString = myAppointment == -1 ? String.valueOf(myAppointment) : "null";
			System.out.println(this.getLocalName() + ": Appointment " + appointmentString);
		}
	}
}