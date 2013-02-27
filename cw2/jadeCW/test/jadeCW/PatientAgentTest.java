package jadeCW;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gdj08
 * Date: 27/02/13
 * Time: 10:32
 * To change this template use File | Settings | File Templates.
 */
public class PatientAgentTest {

    PatientAgent agent;

    @Before
    public void setup(){
        agent = new PatientAgent();
    }
    @Test
    public void testThatInputIsParsedCorrectly(){
        String input = "2 1 - 3 7 6 - 5 -";
        List<ArrayList<Integer>> output = agent.parsePatientAppointmentPreferences(input);
        ArrayList<Integer> list1 = new ArrayList<Integer>(Arrays.asList(2,1));
        ArrayList<Integer> list2 = new ArrayList<Integer>(Arrays.asList(3,7,6));
        ArrayList<Integer> list3 = new ArrayList<Integer>(Arrays.asList(5));
        List<ArrayList<Integer>> expectedOutput = new ArrayList<ArrayList<Integer>>();
        expectedOutput.add(list1);
        expectedOutput.add(list2);
        expectedOutput.add(list3);
        Assert.assertEquals(expectedOutput, output);
    }


    @Test
    public void testThatInputIsParsedCorrectly_blankInput(){
        String input = "";
        List<ArrayList<Integer>> output = agent.parsePatientAppointmentPreferences(input);
        List<ArrayList<Integer>> expectedOutput = new ArrayList<ArrayList<Integer>>();
        expectedOutput.add(new ArrayList<Integer>());
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void testThatInputIsParsedCorrectly_blankInputWithHyphen(){
        String input = "-";
        List<ArrayList<Integer>> output = agent.parsePatientAppointmentPreferences(input);
        List<ArrayList<Integer>> expectedOutput = new ArrayList<ArrayList<Integer>>();
        expectedOutput.add(new ArrayList<Integer>());
        Assert.assertEquals(expectedOutput, output);
    }


}
