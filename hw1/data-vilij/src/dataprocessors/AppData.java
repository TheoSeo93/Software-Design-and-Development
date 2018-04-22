package dataprocessors;

import algorithms.Algorithm;
import algorithms.Clustering;
import classification.Classification;
import classification.RandomClassifier;
import javafx.geometry.Point2D;
import javafx.scene.control.RadioButton;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

import static settings.AppPropertyTypes.*;
import static ui.DataVisualizer.newLineRegex;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor processor;
    private ApplicationTemplate applicationTemplate;
    private StringBuilder pendingText = new StringBuilder();
    private boolean toContinue;
    private String updatedChartData;
    private String wrongDataFormat;
    private String wrongDataFormatContent;
    private String empty;
    private String specified;
    private String wrongExtention;
    private String wrongExtentionContent;


    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
        wrongDataFormat = applicationTemplate.manager.getPropertyValue(WRONG_DATA_FORMAT_ERROR.toString());
        wrongDataFormatContent = applicationTemplate.manager.getPropertyValue(WRONG_DATA_FORMAT_ERROR_CONTENT.toString());
        empty = applicationTemplate.manager.getPropertyValue(EMPTY.toString());
        specified = applicationTemplate.manager.getPropertyValue(SPECIFIED_FILE.toString());
        wrongExtention = applicationTemplate.manager.getPropertyValue(WRONG_EXTENSION.toString());
        wrongDataFormatContent = applicationTemplate.manager.getPropertyValue(WRONG_EXTENSION_CONTENT.toString());
    }

    @Override
    public void loadData(Path dataFilePath) {
        pendingText.setLength(0);
        pendingText.trimToSize();
        processor.setManager(applicationTemplate.manager);
        ((AppUI) applicationTemplate.getUIComponent()).getTextArea().clear();
        try {
            InputStream inputStream = Files.newInputStream(dataFilePath);
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                int counter = 1;
                while ((line = ((BufferedReader) reader).readLine()) != null) {

                    if (counter > 10) {
                        ((AppUI) applicationTemplate.getUIComponent()).setMoreThanTen(true);
                        pendingText.append(line + newLineRegex);

                    } else {
                        ((AppUI) applicationTemplate.getUIComponent()).setMoreThanTen(false);
                        textBuilder.append(line + newLineRegex);

                    }
                    counter++;
                }
            }

            processor.processString(textBuilder.toString());
            updatedChartData = textBuilder.toString();
            String filePath = dataFilePath.toAbsolutePath().toString() + System.lineSeparator();
            updateTextFlow(filePath);
            if (processor.getDataSize() > 10)
                applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(DATA_EXCEEDED.toString(), applicationTemplate.manager.getPropertyValue(DATA_EXCEEDED.toString()) + processor.getDataSize());
            processor.clear();
            ((AppUI) applicationTemplate.getUIComponent()).getTextArea().setText(textBuilder.toString());
            ((AppUI) applicationTemplate.getUIComponent()).setReadOnly();
        } catch (Exception ex) {
            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(LOAD.toString(), ex.getMessage());
            processor.clear();
        }

        ((AppUI) applicationTemplate.getUIComponent()).setPendingText(pendingText);


    }

    public void loadData(String dataString) throws Exception {
        processor.setManager(applicationTemplate.manager);
        processor.processString(dataString);
        displayData();
        ((AppUI) applicationTemplate.getUIComponent()).enableScrnshot();

    }

    public String getUpdatedChartData() {
        return updatedChartData;
    }

    public void setUpdatedChartData(String updatedChartData) {
        this.updatedChartData = updatedChartData;
    }

    @Override
    public void saveData(Path dataFilePath) {
        String chartData = ((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText().toString();
        String title = SAVE_WRONG_DATA.toString();
        updatedChartData = chartData;
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

    public void updateTextFlow(String dataFilePath) {
        TextFlow textFlow = ((AppUI) applicationTemplate.getUIComponent()).getTextFlow();
        if (textFlow.getChildren().size() != 0)
            ((AppUI) applicationTemplate.getUIComponent()).getTextFlow().getChildren().clear();

        textFlow.setLineSpacing(5);
        Text firstLine = new Text(processor.getDataSize() + PropertyManager.getManager().getPropertyValue(FIRSTLINE.toString()));
        Text labelDescription = new Text(processor.getDataLabelCount() + PropertyManager.getManager().getPropertyValue(LABELNAMES.toString()) + System.lineSeparator());
        Text pathDescription = new Text(PropertyManager.getManager().getPropertyValue(FROM.toString()) + System.lineSeparator() + dataFilePath);
        textFlow.getChildren().add(firstLine);
        textFlow.getChildren().add(pathDescription);
        textFlow.getChildren().add(labelDescription);
        Iterator labelIterator = processor.getLabels().iterator();

        for (int i = 0; i < processor.getDataLabelCount(); i++) {
            textFlow.getChildren().add(new Text(labelIterator.next() + System.lineSeparator()));
            labelIterator.remove();
        }
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public void setDataSet(String chartData) throws Exception {
        processor.clear();
        processor.processString(chartData);
    }

    public Map<String, Point2D> getDataPoints() {
        return processor.getDataPoints();
    }

    public Map<String, String> getLabels() {
        return processor.getLabelsAsMap();
    }

    public void displayData() {

        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
        applyAlgorithm();
        ((AppUI) applicationTemplate.getUIComponent()).enableScrnshot();
        processor.clear();

    }

    public void applyAlgorithm() {
        Classification[] classificationConfigs = ((AppUI) applicationTemplate.getUIComponent()).getClassificationConfigs();
        Algorithm[] randomClassifiers = ((AppUI) applicationTemplate.getUIComponent()).getRandomClassifiers();
        Clustering[] clusterConfigs = ((AppUI) applicationTemplate.getUIComponent()).getClusterConfigs();
        RadioButton[] radioButtons = ((AppUI) applicationTemplate.getUIComponent()).getRadioButtons();
        switch (((AppUI) applicationTemplate.getUIComponent()).getAlgorithmType()) {
            case RANDOMCLUSTERING:
//                    for(int i=0;i<3;i++){
//                        if(radioButtons[i].isSelected()){
//                            randomClassifiers[i].run();
//                            break;
//                        }
//                    }
                break;
            case CLASSIFICATION:
                for (int i = 0; i < 3; i++) {
                    if (radioButtons[i].isSelected()) {
                        if (classificationConfigs[i].tocontinue())
                            ((AppUI) applicationTemplate.getUIComponent()).disableState(true);
                        new Thread(classificationConfigs[i]).start();
                        ((AppUI) applicationTemplate.getUIComponent()).disableState(false);
                        break;
                    }
                }
                break;
            case CLUSTERING:
                for (int i = 0; i < 3; i++) {
                    if (radioButtons[i].isSelected()) {
                        clusterConfigs[i].run();
                        break;
                    }
                }
                break;
            case RANDOMCLASSIFIER:
                for (int i = 0; i < 3; i++) {
                    if (radioButtons[i].isSelected()) {
                        ((RandomClassifier) randomClassifiers[i]).setApplicationTemplate(applicationTemplate);
                            new Thread(randomClassifiers[i]).start();
                        break;
                    }
                }
                break;
        }
    }

    public boolean getContinue() {
        return toContinue;
    }

}
