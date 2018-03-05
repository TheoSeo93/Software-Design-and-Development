package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.templates.ApplicationTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

import static settings.AppPropertyTypes.*;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor processor;
    private ApplicationTemplate applicationTemplate;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
        processor.setManager(applicationTemplate.manager);
        ((AppUI) applicationTemplate.getUIComponent()).getTextArea().clear();
        try {
            InputStream inputStream = Files.newInputStream(dataFilePath);
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                int counter = 1;
                while ((line = ((BufferedReader) reader).readLine()) != null) {
                    if (counter > 10)
                        break;
                    textBuilder.append(line + System.lineSeparator());
                    counter++;
                }
            }
            processor.processString(textBuilder.toString());
            if (processor.getDataSize() > 10)
                applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(DATA_EXCEEDED.toString(), applicationTemplate.manager.getPropertyValue(DATA_EXCEEDED.toString()) + processor.getDataSize());

            processor.clear();
            ((AppUI) applicationTemplate.getUIComponent()).getTextArea().setText(textBuilder.toString());
        } catch (Exception ex) {
            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(LOAD.toString(), ex.getMessage());
        }


    }

    public void loadData(String dataString) throws Exception {
        processor.setManager(applicationTemplate.manager);
        processor.processString(dataString);
        displayData();
        ((AppUI) applicationTemplate.getUIComponent()).enableScrnshot();

    }

    @Override
    public void saveData(Path dataFilePath) {
        String chartData = ((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText().toString();
        String title = SAVE_WRONG_DATA.toString();
        processor.setManager(applicationTemplate.manager);
        try {
            processor.clear();
            processor.processString(chartData);
        } catch (Exception ex) {
            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(title, ex.getMessage());
            return;
        }

        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
            writer.write(chartData);
            writer.close();
        } catch (IOException e) {
            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(SAVE_IOEXCEPTION.toString(),
                    applicationTemplate.manager.getPropertyValue(SAVE_IOEXCEPTION.toString()));
        }


    }

    @Override
    public void clear() {
        processor.clear();
    }


    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
        Map<String, Point2D> dataPoints = processor.getDataPoints();
        ArrayList<Point2D> points = new ArrayList<>();
        points.addAll(dataPoints.values());


        ((AppUI) applicationTemplate.getUIComponent()).enableScrnshot();
        processor.clear();

    }


}
