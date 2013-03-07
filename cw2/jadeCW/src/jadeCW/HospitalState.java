package jadeCW;

import jade.core.AID;

public class HospitalState {

    private AID[] appointments;
    
    public HospitalState(int numberOfAppointments) {
        appointments = new AID[numberOfAppointments];
	}
        
    public AID getAppointmentOwner(int appointmentNumber) {
        return appointments[appointmentNumber-1];
    }

	public AID[] getAppointments() {
		return appointments;
	}

	public void setAppointment(int i, AID patient) {
		appointments[i-1] = patient;
	}

	public int getNextAvailableAppointment() {
		for (int i = 0; i < appointments.length; i++){
        	if (appointments[i] == null){
        		return i;
        	}
        }
        return -1;
	}

	public boolean isAppointmentFree(int receiverAppointment) {
		if (receiverAppointment - 1 >= 0) {
			return appointments[receiverAppointment-1] == null;
		}
		return false;
	}

	public int setNextAvailableAppointment(AID patient) {
		for (int i = 0; i < appointments.length; i++){
        	if (appointments[i] == null){
        		appointments[i] = patient;
        		return i;
        	}
        }
		return -1;
	}
}
