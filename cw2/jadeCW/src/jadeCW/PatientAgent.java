package jadeCW;

import jade.core.Agent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gdj08
 * Date: 26/02/13
 * Time: 10:39
 * To change this template use File | Settings | File Templates.
 */
public class PatientAgent extends Agent {

    protected void setup() {

        List<ArrayList<Integer>> appointmentPreferences = new ArrayList<ArrayList<Integer>>();

        String inputString = System.console().readLine().trim();

        String[] splitString = inputString.split("-");

        for (int i = 0; i < splitString.length; i++) {
            String[] splitPrefRow = splitString[i].split(" ");
            ArrayList<Integer> row = new ArrayList<Integer>();
            for (int j = 0; j < splitPrefRow.length; j++) {
                if (!splitPrefRow[j].equals(" ") || !splitPrefRow[j].equals("")) {
                    row.add(Integer.parseInt(splitPrefRow[j]));
                }
            }
            appointmentPreferences.add(row);
        }

        doDelete();

    }

}
