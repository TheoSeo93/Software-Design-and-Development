package actions;

import dataprocessors.TSDProcessor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ui.DataVisualizer;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.templates.ApplicationTemplate;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.nio.file.Path;

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
            if (promptToSave() == true)
                handleSaveRequest();
        } catch (Exception ex) {
            if(ex instanceof  IOException)
            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show("File not saved", "Cannot save the file");
        }
        applicationTemplate.getDataComponent().clear();

    }

    @Override
    public void handleSaveRequest() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("tsd files (*.tsd)", "*.tsd"));
        fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
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
        confirmationDialog.show("Save Current Work", "Would you like to save current work?");
        if (confirmationDialog.getSelectedOption() == ConfirmationDialog.Option.CANCEL)
            return false;

        else if (confirmationDialog.getSelectedOption() == ConfirmationDialog.Option.YES) {
            return true;
        } else {
            applicationTemplate.getDataComponent().clear();
            return false;
        }
    }
}
