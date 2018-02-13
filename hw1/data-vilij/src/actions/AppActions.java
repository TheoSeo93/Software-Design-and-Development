package actions;



import javafx.application.Platform;
import javafx.stage.FileChooser;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.templates.ApplicationTemplate;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static settings.AppPropertyTypes.*;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /**
     * The application to which this class of actions belongs.
     */
    private ApplicationTemplate applicationTemplate;
    /**
     * Path to the data file currently active.
     */
    Path dataFilePath;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;

    }

    @Override
    public void handleNewRequest() {
        try {
            if (promptToSave())
                handleSaveRequest();

        } catch (Exception ex) {
            if (ex instanceof IOException)
                applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(applicationTemplate.manager.getPropertyValue(SAVE_IOEXCEPTION.toString()),
                        applicationTemplate.manager.getPropertyValue(SAVE_IOEXCEPTION.toString()));
        }
    }

    @Override
    public void handleSaveRequest() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_TITLE.toString()));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.toString()),
                        applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.toString())));

        File file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

        if (file != null) {
            try {
                String chartData = ((AppUI) applicationTemplate.getUIComponent()).getTextArea().textProperty().toString();
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(chartData);
                fileWriter.close();
            } catch (IOException ex) {
                applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(applicationTemplate.manager.getPropertyValue(SAVE_IOEXCEPTION.toString()), applicationTemplate.manager.getPropertyValue(SAVE_IOEXCEPTION.toString()));
            }
        }


    }

    @Override
    public void handleLoadRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleExitRequest() {
        Platform.exit();
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        // TODO: NOT A PART OF HW 1
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        ConfirmationDialog confirmationDialog = (ConfirmationDialog) applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
        confirmationDialog.show(applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.toString()), applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK.toString()));
        if (confirmationDialog.getSelectedOption() == ConfirmationDialog.Option.CANCEL) {
            return false;
        } else if (confirmationDialog.getSelectedOption() == ConfirmationDialog.Option.YES) {
            return true;
        } else {
            applicationTemplate.getDataComponent().clear();
            applicationTemplate.getUIComponent().clear();
            return false;
        }
    }
}
