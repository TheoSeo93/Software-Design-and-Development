package dataprocessors;

import javafx.stage.Stage;
import org.junit.Test;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import javax.xml.soap.Text;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static ui.DataVisualizer.newLineRegex;

/**
 * Saving data from the UI text-area to a .tsd file.
 * Test 1:  Valid data sets
 * Test 2:  Wrong data path passed, exception thrown
 * Test 3: "Instance3	label3	1.5,15"
 * Test 4: "@instance4  label4  1,4"
 *
 * @param
 * @author Theo Seo
 * @version 1.0
 */
public class AppDataTest {
    /**
     *  Test 1: Valid data set are provided, then check each line by reading each line from the saved file..
     *  Test 2 :Invalid data sets are provided.
     *   Expected Exception : Exception.class
     */
    @Test
    public void saveData1() {

        //Instantiating file to be saved
        File file = new File("hw1/data-vilij/resources/data/testFile.tsd");
        Path path = file.toPath();
        AppData appData = new AppData();
        String chartData = "@Instance1\tlabel1\t1.5,2.2\n" +
                "@Instance2\tlabel1\t1.8,3\n" +
                "@Instance3\tlabel1\t2.1,2.9\n" +
                "@Instance4\tlabel2\t10,9.4\n" +
                "@Instance5\tlabel2\t10,9.5\n" +
                "@Instance6\tlabel2\t11,42\n";
        appData.saveData(path, chartData);
        try {
            boolean result = true;
            String[] array = chartData.split(newLineRegex);
            for (int i = 0; i < array.length; i++) {
                System.out.print(array[i]);
            }
            Scanner read = new Scanner(file);
            int i;
            for (i = 0; read.hasNextLine() || i < array.length; i++) {
                if (!array[i].equals(read.nextLine())) {
                    result = false;
                }
            }
            assertEquals(true, result && (array.length == i));
        } catch (IOException ex) {
            fail();
        }
    }

    @Test(expected = Exception.class)
    public void saveData2() {
        //Instantiating file to be saved
        File file = new File("no such path");
        Path path = file.toPath();
        AppData appData = new AppData();
        String chartData = "@Instance1\tlabel1\t1.5,2.2\n" +
                "Instance2\tlabel1\t1.8,3\n" +
                "@Instance3\tlabel1\t2.1,2.9\n" +
                "@Instance4\tlabel2\t10,9.4\n" +
                "@Instance5\tlabel2\t10,9.5\n" +
                "@Instance6\tlabel2\t11,42\n";
        appData.saveData(path, chartData);
        try {
            boolean result = true;
            String[] array = chartData.split(newLineRegex);
            for (int i = 0; i < array.length; i++) {
                System.out.print(array[i]);
            }
            Scanner read = new Scanner(file);
            int i;
            for (i = 0; read.hasNextLine() || i < array.length; i++) {
                if (!array[i].equals(read.nextLine())) {
                    result = false;
                }
            }
            assertEquals(true, result && (array.length == i));
        } catch (IOException ex) {
            fail();
        }
    }
}