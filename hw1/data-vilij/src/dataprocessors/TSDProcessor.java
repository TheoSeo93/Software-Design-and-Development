package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import ui.AppUI;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


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

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    private Map<String, String> dataLabels;
    private Map<String, Point2D> dataPoints;

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
        Stream.of(AppUI.regexString)
                .map(line -> Arrays.asList(line.split("[\t]")))
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
        StringBuilder errorMessage = new StringBuilder();
        StringBuilder invalidNameFlag = new StringBuilder();
        Stream.of(tsdString.split("\n"))
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
                            dataLabels.put(name, label);
                            dataPoints.put(name, point);
                        } else {
                            List list = Arrays.asList(line.split("\\s+"));
                            if (!firstMatch.find()) {
                                invalidNameFlag.append(list.get(0));
                                invalidNameFlag.append(checkedname(list.get(0).toString()));
                            }

                            throw new Exception();
                        }

                    } catch (Exception e) {

                        errorMessage.setLength(0);
                        errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                        notError.set(false);
                        System.out.print(invalidNameFlag.toString());
                    }

                });
        if (invalidNameFlag.toString().length() > 0){
            throw new InvalidDataNameException(invalidNameFlag.toString());
        }
        else if (errorMessage.length() > 0)
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
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY()));
            });

            chart.getData().add(series);
        }
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }
}