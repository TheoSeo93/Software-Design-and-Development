package dataprocessors;

import javafx.geometry.Point2D;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.fail;


/**
 * Parsing a single line of data in the TSD format to create an instance object (must include at least two boundary-value test cases).
 * For practicing regular expression, I TSD processor also allows space-separated format.
 * <p>
 * Test 1: "@Instance1	label1	1.5,15"
 * Test 2: "@Instance2 123	label2	1.5,15"
 * Test 3: "Instance3	label3	1.5,15"
 * Test 4: "@instance4  label4  1,4"
 *
 * @param
 * @author Theo Seo
 * @version 1.0
 */
public class TSDProcessorTest {
    /**
     *  Test 1: "@Instance1	label1	1.5,15": [Valid test case]
     *  @exception Exception
     *  Creates an instance of (1.5,15), labeled as "label1"
     */
    @org.junit.Test
    public void processString1() throws Exception {
        String test1 = "@Instance1	label1	1.5,15";
        TSDProcessor tsdProcessor = new TSDProcessor();
        tsdProcessor.processString(test1);
        Point2D pointLocation = tsdProcessor.getDataPoints().get("Instance1");
        HashSet labels = tsdProcessor.getLabels();
        boolean result = (pointLocation.getX() == 1.5 && pointLocation.getY() == 15) && labels.iterator().next().toString().equals("label1");
        Assert.assertEquals(true, result);
    }

    /**
     *  Test 2: "@Instance2 123	label2	1.5,15": [Invalid test case]
     *  @exception Exception .. Expected Exception : Exception.class
     */
    @Test(expected = Exception.class)
    public void processString2() throws Exception {
        String test1 = "Instance2 asd123\tlabel2\t1.5,15";
        TSDProcessor tsdProcessor = new TSDProcessor();
        tsdProcessor.processString(test1);
        fail();
    }

    /**
     *  Test 3: "Instance3	label3	1.5,15": [Invalid test case]
     *  @exception Exception .. Expected Exception : Exception.class
     */
    @Test(expected = Exception.class)
    public void processString3() throws Exception {
        TSDProcessor tsdProcessor = new TSDProcessor();
        tsdProcessor.processString("Instance3\tlabel3\t1.5,15");
        fail();
    }
    /**
     *  Test 4: "@instance4  label4  1,4": [Valid test case]
     *  @exception Exception
     *  Creates an instance of (1,4"), labeled as "label4"
     */
    @org.junit.Test
    public void processString4() throws Exception {
        String test1 = "@instance4  label4  1,4";
        TSDProcessor tsdProcessor = new TSDProcessor();
        tsdProcessor.processString(test1);
        Point2D pointLocation = tsdProcessor.getDataPoints().get("instance4");
        HashSet labels = tsdProcessor.getLabels();
        boolean result = (pointLocation.getX() == 1 && pointLocation.getY() == 4) && labels.iterator().next().toString().equals("label4");
        Assert.assertEquals(true, result);
    }
}