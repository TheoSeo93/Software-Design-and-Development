package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.stage.Stage;
import vilij.templates.ApplicationTemplate;

import static vilij.settings.InitializationParams.*;


/**
 * The main class from which the application is run. The various components used here must be concrete implementations
 * of types defined in {@link vilij.components}.
 *
 * @author Ritwik Banerjee
 */
public final class DataVisualizer extends ApplicationTemplate {
    public static String regexString = "^@[^\\s]+[a-zA-Z0-9]*[\\s]+[a-zA-Z0-9]+[\\s]+?([0-9]*[.])?[0-9]+,+?([0-9]*[.])?[0-9]+\\s*$\t(?<=@)[a-zA-Z0-9]*(?!:(\\s*[a-zA-Z0-9]*[+-]?([0-9]*[.])?[0-9]+,[+-]?([0-9]*[.])?[0-9]+))\t(?<=[a-zA-Z0-9]{1,20})\\s+[a-zA-Z0-9]*(?=(\\s+(?<=\\s{1,4})[+-]?([0-9]*[.])?[0-9]+,[+-]?([0-9]*[.])?[0-9]+))\t(?<=[a-zA-Z0-9])\\s+?([0-9]*[.])?[0-9]+(?<!,\\d)\t(?<=[a-zA-Z0-9]\\s{1,20}?([0-9]{1,4}[.])?[0-9]{1,4},)([0-9]*[.])?[0-9]+\t";
    public static String at = "@";
    public static String newLineRegex ="\n";
    public static String tabRegex="[\t]";
    public static String spaceRegex="\\s+";
    @Override
    public void start(Stage primaryStage) {
        dialogsAudit(primaryStage);
        if (propertyAudit())
            userInterfaceAudit(primaryStage);
    }

    @Override
    protected boolean propertyAudit() {
        boolean failed = manager == null || !(loadProperties(PROPERTIES_XML) && loadProperties(WORKSPACE_PROPERTIES_XML));
        if (failed)
            errorDialog.show(LOAD_ERROR_TITLE.getParameterName(), PROPERTIES_LOAD_ERROR_MESSAGE.getParameterName());
        return !failed;
    }

    @Override
    protected void userInterfaceAudit(Stage primaryStage) {
        setUIComponent(new AppUI(primaryStage, this));
        setActionComponent(new AppActions(this));
        setDataComponent(new AppData(this));

        uiComponent.initialize();
    }

}
