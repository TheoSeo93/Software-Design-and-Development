package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;

import java.nio.file.AtomicMoveNotSupportedException;
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

        String entireFormat = "^@[^\\s]+[a-zA-Z0-9]*[\\s]+[a-zA-Z0-9]+[\\s]+?([0-9]*[.])?[0-9]+,+?([0-9]*[.])?[0-9]+\\s*$";
        String nameRegex = "(?<=@)[a-zA-Z0-9]*(?!:(\\s*[a-zA-Z0-9]*[+-]?([0-9]*[.])?[0-9]+,[+-]?([0-9]*[.])?[0-9]+))";
        String labelRegex = "(?<=[a-zA-Z0-9]{1,20})\\s+[a-zA-Z0-9]*(?=(\\s+(?<=\\s{1,4})[+-]?([0-9]*[.])?[0-9]+,[+-]?([0-9]*[.])?[0-9]+))";
        String xposRegex = "(?<=[a-zA-Z0-9])\\s+?([0-9]*[.])?[0-9]+(?<!,\\d)";
        String yposRegex = "(?<=[a-zA-Z0-9]\\s{1,20}?([0-9]{1,4}[.])?[0-9]{1,4},)([0-9]*[.])?[0-9]+";

        Pattern formatPattern = Pattern.compile(entireFormat);
        Pattern namePattern = Pattern.compile(nameRegex);
        Pattern labelPattern = Pattern.compile(labelRegex);
        Pattern xPattern = Pattern.compile(xposRegex);
        Pattern yPattern = Pattern.compile(yposRegex);

        final AtomicBoolean notError = new AtomicBoolean(true);
        StringBuilder errorMessage = new StringBuilder();
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
                            Point2D point = new Point2D(Double.valueOf(thirdMatch.group(0)), Double.parseDouble(fourthMatch.group(0)));
                            dataLabels.put(name, label);
                            dataPoints.put(name, point);
                        } else {
                            if (!firstMatch.find()) {
                                List list = Arrays.asList(line.split("\\s+"));
                                throw new InvalidDataNameException(checkedname((String) list.get(0)));
                            } else
                                throw new Exception("It's not in the following format: [@name][space][label][space][xPos,yPos]");

                        }
                    } catch (Exception e) {

                        errorMessage.setLength(0);
                        errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
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