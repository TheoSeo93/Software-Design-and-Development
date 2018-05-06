package ui;

import algorithms.Algorithm;
import dataprocessors.AppData;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Run configuration for algorithms (must include one boundary value test case for every field in the configuration).
 * This is the logic actually used for the text watcher from the configurations dialog.
 * Since the way that the text watcher deals with the input values are dynamic, and JUnit cannot test methods with Javafx nodes, this is slightly different from the actual algorithm I implemented.
 *
 *
 * RandomClassifier Test 1: MaxIterations: -1, Update Interval : 10 [Boundary]
 * RandomClassifier Test 2: MaxIterations: 10, Update Interval : -1 [Boundary]
 * RandomClassifier Test 3: MaxIterations: 7,  Update Interval : 5  [Valid]
 * RandomClassifier Test 4: MaxIterations: 7,  Update Interval : a  [Invalid]
 * Boundary value choices: MaxIteration<=0 || updateInterval<=0 || MaxIteration < updateInterval
 *
 * RandomClusterer Test 1: MaxIterations: 10, Update Interval : 5  ClusterNumbers : 0  [Boundary]
 * RandomClusterer Test 2: MaxIterations: 10, Update Interval : 5  ClusterNumbers : 5  [Boundary]
 * RandomClusterer Test 3: MaxIterations: a,  Update Interval : 5   ClusterNumbers :4  [Invalid]
 * Boundary value choices: ClusterNumbers<=1 || ClusterNumbers>4 ||MaxIteration<=0 || updateInterval<=0 || MaxIteration < updateInterval
 *
 * KMeansClusterer Test 1: MaxIterations: 6, Update Interval : 4  ClusterNumbers :2  [Valid]
 * KMeansClusterer Test 2: MaxIterations: 5, Update Interval : 4  ClusterNumbers : 7 Boundary]
 * KMeansClusterer Test 3: MaxIterations: 7,  Update Interval : a   ClusterNumbers :0 [Invalid]
 * Boundary value choices: ClusterNumbers<=1 || ClusterNumbers>4 ||MaxIteration<=0 || updateInterval<=0 || MaxIteration < updateInterval
 *
 * @author Theo Seo
 * @version 1.0
 */
public class AppUITest {

    /**
     *  Test 1: RandomClassifier
     *  Invalid Input
     *  MaxIterations = 0
     *  UpdateInterval = 10
     *  This is the case of boundary value.
     *  Max iterations will be set to 1, and this will cause the update interval to be changed because update interval cannot exceed the max iterations.
     */
    @Test
    public void validText1() {
        boolean isValid;
        String[] textFields = {"0", "10"};
        isValid = AppUI.validText1(textFields, Algorithm.AlgorithmType.RANDOMCLASSIFIER);
        assertEquals(true, !isValid && (Integer.valueOf(textFields[0]) == 1&&Integer.valueOf(textFields[1])==1));
    }
    /**
     *  Test 2: RandomClassifier
     *  Invalid Input
     *  MaxIterations = 10
     *  UpdateInterval = -1
     *  This is the case of boundary value.
     *  UpdateInterval will be set to 1.
     */
    @Test
    public void validText2() {
        boolean isValid;
        String[] textFields = {"10", "-1"};
        isValid = AppUI.validText1(textFields, Algorithm.AlgorithmType.RANDOMCLASSIFIER);
        assertEquals(true, !isValid && (Integer.valueOf(textFields[0]) == 10&&Integer.valueOf(textFields[1])==1));
    }
    /**
     *  Test 3: RandomClassifier
     *  MaxIterations = 7
     *  UpdateInterval = 5
     *  Valid Input
     *  UpdateInterval will be set to 1.
     */
    @Test
    public void validText3() {
        boolean isValid;
        String[] textFields = {"7", "5"};
        isValid = AppUI.validText1(textFields, Algorithm.AlgorithmType.RANDOMCLASSIFIER);
        assertEquals(true, isValid && (Integer.valueOf(textFields[0]) == 7&&Integer.valueOf(textFields[1])==5));
    }
    /**
     *  Test 4: RandomClassifier
     *  MaxIterations = 7
     *  UpdateInterval = a
     *  InValid Input
     *  NumberFormatException will be thrown.
     */
    @Test(expected = NumberFormatException.class)
    public void validText4() {
        boolean isValid;
        String[] textFields = {"7", "a"};
        isValid = AppUI.validText1(textFields, Algorithm.AlgorithmType.RANDOMCLASSIFIER);
        assertEquals(true, isValid && (Integer.valueOf(textFields[0]) == 7&&Integer.valueOf(textFields[1])==5));
    }
    /**
     *  Test 5: RandomClusterer
     *  Boundary
     *  MaxIterations = 10
     *  UpdateInterval = 5
     *  Clusters = 0
     *  This is the case of boundary value.
     *  Clusters will be set to 2.
     */
    @Test
    public void validText5() {
        boolean isValid;
        String[] textFields = {"10", "5", "0"};
        isValid = AppUI.validText1(textFields, Algorithm.AlgorithmType.RANDOMCLUSTERER);
        assertEquals(true, (!isValid) && (Integer.valueOf(textFields[0]) == 10&& Integer.valueOf(textFields[1])==5&& Integer.valueOf(textFields[2])==2));
    }
    /**
     *  Test 6: RandomClassifier
     *  Invalid Input
     *  MaxIterations = 10
     *  UpdateInterval = 5
     *  Clusters = 5
     *  This is the case of boundary value.
     *  Clusters will be set to 4.
     */
    @Test
    public void validText6() {
        boolean isValid;
        String[] textFields = {"10", "5", "5"};
        isValid = AppUI.validText1(textFields, Algorithm.AlgorithmType.RANDOMCLUSTERER);
        assertEquals(true, (!isValid) && (Integer.valueOf(textFields[0]) == 10&& Integer.valueOf(textFields[1])==5&& Integer.valueOf(textFields[2])==4));
    }
    /**
     *  Test 7: RandomClassifier
     *  Invalid Input
     *  MaxIterations = a
     *  UpdateInterval = 5
     *  Clusters = 4
     *  This is the case of Invalid value.
     */
    @Test(expected = NumberFormatException.class)
    public void validText7() {
        boolean isValid;
        String[] textFields = {"a", "5","4"};
        isValid = AppUI.validText1(textFields, Algorithm.AlgorithmType.RANDOMCLUSTERER);
        assertEquals(true, isValid && (Integer.valueOf(textFields[0]) == 7&&Integer.valueOf(textFields[1])==5));
    }
    /**
     *  Test 8: KMEANSCLUSTERER
     *  Invalid Input
     *  MaxIterations = 6
     *  UpdateInterval = 4
     *  Clusters = 2
     *  This is the case of valid value.
     */
    @Test
    public void validText8() {
        boolean isValid;
        String[] textFields = {"6", "4","2"};
        isValid = AppUI.validText1(textFields, Algorithm.AlgorithmType.KMEANSCLUSTERER);
        assertEquals(true, (isValid) && (Integer.valueOf(textFields[0]) == 6&& Integer.valueOf(textFields[1])==4&& Integer.valueOf(textFields[2])==2));
    }
    /**
     *  Test 9: KMEANSCLUSTERER
     *  Invalid Input
     *  MaxIterations = 5
     *  UpdateInterval = 4
     *  Clusters = 7
     *  This is the case of boundary value.
     *  Clusters are set to the upper bound value 4
     */
    @Test
    public void validText9() {
        boolean isValid;
        String[] textFields = {"5", "4","7"};
        isValid = AppUI.validText1(textFields, Algorithm.AlgorithmType.KMEANSCLUSTERER);
        System.out.print(textFields[2]);
        assertEquals(true, (!isValid) && (Integer.valueOf(textFields[0]) == 5&& Integer.valueOf(textFields[1])==4&& Integer.valueOf(textFields[2])==4));
    }
    /**
     *  Test 10: KMEANSCLUSTERER
     *  MaxIterations = 7
     *  UpdateInterval = 5
     *  Valid Input
     *  UpdateInterval will be set to 1.
     */
    @Test(expected = NumberFormatException.class)
    public void validText10() {
        boolean isValid;
        String[] textFields = {"7", "4","a"};
        isValid = AppUI.validText1(textFields, Algorithm.AlgorithmType.KMEANSCLUSTERER);
        assertEquals(true, isValid && (Integer.valueOf(textFields[0]) == 7&&Integer.valueOf(textFields[1])==5));
    }

}