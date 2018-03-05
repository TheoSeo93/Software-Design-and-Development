package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import ui.AppUI;
import vilij.propertymanager.PropertyManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static settings.AppPropertyTypes.*;
import static ui.DataVisualizer.*;


/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {
        private static String error ="Invalid name '%s'";

        private static String onHover="onHover";
    public static class InvalidDataNameException extends Exception {

        public InvalidDataNameException(String name, String wrongPos) {
            super(String.format(error + manager.getPropertyValue(NAME_ERROR_MSG.toString()) + wrongPos, name));
        }
    }

    private Map<String, String> dataLabels;
    private Map<String, Point2D> dataPoints;
    private static PropertyManager manager;

    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {

        String[] regex = new String[5];
        Stream.of(regexString)
                .map(line -> Arrays.asList(line.split(tabRegex)))
                .forEach(list -> {
                    regex[0] = list.get(0);
                    regex[1] = list.get(1);
                    regex[2] = list.get(2);
                    regex[3] = list.get(3);
                    regex[4] = list.get(4);
                });

        String entireFormat = regex[0];
        String nameRegex = regex[1];
        String labelRegex = regex[2];
        String xposRegex = regex[3];
        String yposRegex = regex[4];

        Pattern formatPattern = Pattern.compile(entireFormat);
        Pattern namePattern = Pattern.compile(nameRegex);
        Pattern labelPattern = Pattern.compile(labelRegex);
        Pattern xPattern = Pattern.compile(xposRegex);
        Pattern yPattern = Pattern.compile(yposRegex);
        final AtomicBoolean notError = new AtomicBoolean(true);
        AtomicInteger lineNumber = new AtomicInteger(1);
        StringBuilder errorMessage = new StringBuilder();

        Stream.of(tsdString.split(newLineRegex))
                .forEach(line -> {
                    try {
                        Matcher formatMatch = formatPattern.matcher(line);
                        Matcher firstMatch = namePattern.matcher(line);
                        Matcher secondMatch = labelPattern.matcher(line);
                        Matcher thirdMatch = xPattern.matcher(line);
                        Matcher fourthMatch = yPattern.matcher(line);
                        notError.set(formatMatch.matches() && firstMatch.find() && secondMatch.find() && thirdMatch.find() && fourthMatch.find());
                        if (notError.get()) {
                            String name = firstMatch.group(0);
                            String label = secondMatch.group(0);
                            name = name.trim();
                            label = label.trim();
                            Point2D point = new Point2D(Double.valueOf(thirdMatch.group(0)), Double.parseDouble(fourthMatch.group(0)));
                            if (!dataLabels.containsKey(name)) {
                                dataLabels.put(name, label);
                                dataPoints.put(name, point);
                            } else {
                                errorMessage.append(manager.getPropertyValue(DUPLICATE_NAME.toString()) + manager.getPropertyValue(AT.toString()));
                                errorMessage.append(name);
                                errorMessage.append(manager.getPropertyValue(ERROR_POSITION.toString()));
                                errorMessage.append(lineNumber.get() + System.lineSeparator());
                            }
                            lineNumber.getAndIncrement();
                        } else {
                            List list = Arrays.asList(line.split(spaceRegex));
                            if (!firstMatch.find()) {
                                errorMessage.append(checkedname(list.get(0).toString(), manager.getPropertyValue(ERROR_POSITION.toString()) + lineNumber));
                            } else if (manager != null) {
                                errorMessage.append(manager.getPropertyValue(WRONG_DATA_FORMAT_ERROR_CONTENT.toString()));
                                errorMessage.append(manager.getPropertyValue(ERROR_POSITION.toString()));
                                errorMessage.append(lineNumber.get() +  System.lineSeparator());
                            }
                            lineNumber.getAndIncrement();
                        }

                    } catch (Exception e) {
                        lineNumber.getAndIncrement();
                        errorMessage.append(e.getMessage() +  System.lineSeparator());
                        notError.set(false);
                    }
                });

        if (errorMessage.length() > 0)
            throw new Exception(errorMessage.toString());
    }


    public void update() {
        clear();
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    public void toChartData(XYChart<Number, Number> chart) {

        ArrayList<Point2D> points = new ArrayList<>();
        points.addAll(dataPoints.values());
        double startX=points.get(0).getX();
        double endX =0;
        double avg=0;

        for(int i=0;i<points.size();i++) {
            avg += points.get(i).getY();
            if(endX<points.get(i).getX())
                endX=points.get(i).getX();
            if(startX>points.get(i).getX())
                startX=points.get(i).getX();
        }

        avg/=points.size();
        XYChart.Series<Number,Number> avgSeries = new XYChart.Series<>();

        avgSeries.getData().add(new XYChart.Data<>(startX-10,avg));
        avgSeries.getData().add(new XYChart.Data<>(endX+10,avg));

        avgSeries.setName(manager.getPropertyValue(AVG.toString()));

        chart.getData().add(0,avgSeries);

        Set<String> labels = new HashSet<>(dataLabels.values());

        int counter = 1;
        for (String label : labels) {
            if (counter > 10) {

                break;
            }
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);

            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                XYChart.Data<Number,Number> data = new XYChart.Data<>(point.getX(), point.getY());
                series.getData().add(data);
                data.setExtraValue(entry.getKey());
            });
            chart.getData().add(series);
            counter++;
        }


        Tooltip toolTip = new Tooltip();
        int count=0;
        for (XYChart.Series<Number, Number> series: chart.getData()) {
            if(count==0) {
                count++;
                continue;
            }
            for (XYChart.Data<Number, Number> data : series.getData()) {

                toolTip.install(data.getNode(), new Tooltip(data.getExtraValue().toString()+manager.getPropertyValue(XPOS.toString())+data.getXValue()+manager.getPropertyValue(YPOS.toString())+data.getYValue()));
                data.getNode().setOnMouseEntered(event -> data.getNode().getStyleClass().add(onHover));
                data.getNode().setOnMouseExited(event -> data.getNode().getStyleClass().remove(onHover));

            }

        }


    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private String checkedname(String name, String wrongPos) throws InvalidDataNameException {
        if (!name.startsWith(at))
            throw new InvalidDataNameException(name, wrongPos);
        return name;
    }


    public void setManager(PropertyManager manager) {
        this.manager = manager;
    }

    public int getDataSize() {
        return dataPoints.size();
    }

    public Map<String, Point2D> getDataPoints() {
        return dataPoints;
    }
}
