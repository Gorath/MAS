package jadeCW;

import jade.core.AID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PatientState {

    private HashMap<Integer, AID> knownAppointmentOwners;
    private int myAppointment;
	private List<List<Integer>> appointmentPreferences;
	private AID appointmentAllocator;
	private boolean currentlyProposing;
    
    public PatientState(List<List<Integer>> appointmentPreferences) {
		this.appointmentPreferences = appointmentPreferences;
		knownAppointmentOwners = new HashMap<Integer, AID>();		
		myAppointment = -1;
		currentlyProposing = false;
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
    
    public boolean isAppointmentBetterThanCurrent(int appointment){
    	for (int i = 0; i < appointmentPreferences.size(); i++){
    		List<Integer> currentPreferenceLevelList = appointmentPreferences.get(i);
    		if (currentPreferenceLevelList.contains(appointment)){
    			return true;
    		}
    		else if (currentPreferenceLevelList.contains(myAppointment)) {
            	return false;
            }
    	}
    	return true;
    }
     
    public boolean hasAppointment() {
        return myAppointment != -1;
    }

    public void setAppointment(int myAppointment) {
        this.myAppointment = myAppointment;
    }

    public int getMyAppointment(){
    	return myAppointment;
    }

	public void setAppointmentAllocator(AID appointmentAllocator) {
		this.appointmentAllocator = appointmentAllocator;	
	}

	public AID getAppointmentAllocator() {
		return appointmentAllocator;
	}

	public void setCurrentlyProposing(boolean startProposing){
		this.currentlyProposing = startProposing;
	}
	
	public boolean isCurrentlyProposing() {
		return currentlyProposing;
	}

	public void addAppointmentOwner(int mostPreferred, AID patientAID) {
		knownAppointmentOwners.put(mostPreferred, patientAID);
	}

	public AID getAppointmentOwner(int preferredAppointment) {
		return knownAppointmentOwners.get(preferredAppointment);
	}

	public void removeKnownAppointmentOwner(int newAppointment) {
		knownAppointmentOwners.remove(newAppointment);
	}	
}
