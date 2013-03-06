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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: gdj08
 * Date: 26/02/13
 * Time: 10:39
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class PatientAgent extends Agent {

    private AID appointmentAllocatorProvider;
    private int myAppointment = -1;
    private String inputString;
    private List<ArrayList<Integer>> appointmentPreferences;
    private AID currentMostPreferredAppointmentOwner;
    private List<Integer> availableAppointments;
    private int currentMostPreferredAppointment = -1;
    
	private List<Integer> preferredAlreadyTried = new ArrayList<Integer>();

    public PatientAgent(){
        availableAppointments = new ArrayList<Integer>();
    }

    protected void setup() {

        inputString = "";

        Object[] args = getArguments();
        if (args != null && args.length > 0) {

        	for (Object arg : args){  //THIS is a hack, need to make a change
        		inputString += (String) arg + " ";
        	}
        	System.out.println("Input String: " + inputString);
        }

        parsePatientAppointmentPreferences(inputString);
        subscribeToDF();

        addBehaviour(new RequestAppointment());
        addBehaviour(new FindAppointmentOwner());
        addBehaviour(new ProposeSwap());
    }

    public int getMorePreferredAppointment() {

        for (int i = 0; i < appointmentPreferences.size(); i++) {

            List<Integer> currentPreferenceLevel = appointmentPreferences.get(i);
            if (currentPreferenceLevel.contains(myAppointment)) {
            	// if we go to a preference level which contains our current appointment, then we return -1
            	return -1;
            }

            for (int j = 0; j < currentPreferenceLevel.size(); j++) {
                int preferenceNumber = currentPreferenceLevel.get(j);
                if (!preferredAlreadyTried.contains(preferenceNumber)) {
                    preferredAlreadyTried.add(preferenceNumber);
                    return preferenceNumber;
                }
            }
        }
        return -1;
    }
    
    public List<Integer> getMorePreferredAppointments(){
    	List<Integer> newList = new ArrayList<Integer>();
    	
    	for (int i = 0; i < appointmentPreferences.size(); i++){
    		List<Integer> currentPreferenceLevelList = appointmentPreferences.get(i);
            if (currentPreferenceLevelList.contains(myAppointment)) {
            	// if we go to a preference level which contains our current appointment, then we return the current list
            	return newList;
            }
            //essentially flatten the appointmentPreferences list to the newList
            newList.addAll(currentPreferenceLevelList);
    	}
    	return newList;
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
                                if (sd.getType().equals("allocate-appointments") && appointmentAllocatorProvider == null) {
                                    System.out.println("Appointment allocator found");
                                    System.out.println("- Service \""+sd.getName()+"\" provided by agent "+provider.getName());
                                    // Stores the allocator provider agent for later use
                                    appointmentAllocatorProvider = provider;
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

    public List<ArrayList<Integer>> parsePatientAppointmentPreferences(String line) {
        appointmentPreferences = new ArrayList<ArrayList<Integer>>();

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

    public boolean hasAppointment() {
        return myAppointment != -1;
    }

    public void setAppointment(int myAppointment) {
        this.myAppointment = myAppointment;
    }

    public int getAppointment(){
    	return myAppointment;
    }
    
    public AID getAppointmentAllocator() {
        return appointmentAllocatorProvider;
    }
    
    public void takeDown(){
    	String appointmentString = myAppointment == -1 ? String.valueOf(myAppointment) : "null";
    	System.out.println(this.getLocalName() + ": Appointment " + appointmentString);
    }

    public void setCurrentMostPreferredAppointmentOwner(AID patient){
    	currentMostPreferredAppointmentOwner = patient;
    }
    
    public AID getCurrentMostPreferredAppointmentOwner(){
    	return currentMostPreferredAppointmentOwner;
    }
    
    public void addAvailableAppointment(int appointment){
    	availableAppointments.add(appointment);
    }
    
    public boolean hasMostPreferredAppointmentOwner(){
    	return currentMostPreferredAppointmentOwner != null;
    }
    
    public void setMostPreferredAppointment(int appointment){
    	currentMostPreferredAppointment = appointment;
    }
    
    public int getMostPreferredAppointment(){
    	return currentMostPreferredAppointment;
    }
}
