package actions;


import classification.RandomClassifier;
import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Path;
import java.util.Optional;

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

    private boolean isSaved;
    private File file;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;

    }


    @Override
    public void handleNewRequest() {
        try {
            if (promptToSave()) {
                handleSaveRequest();
            }
        } catch (Exception ex) {
            if (ex instanceof IOException)
                applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(applicationTemplate.manager.getPropertyValue(SAVE_IOEXCEPTION.toString()),
                        applicationTemplate.manager.getPropertyValue(SAVE_IOEXCEPTION.toString()));
        }

        ((AppData) applicationTemplate.getDataComponent()).setUpdatedChartData(PropertyManager.getManager().getPropertyValue(EMPTY.name()));

    }


    @Override
    public void handleSaveRequest() {
        if (!isSaved) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_TITLE.toString()));
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.toString()),
                            applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.toString())));
            File initFile = new File(applicationTemplate.manager.getPropertyValue(DATA_RESOURCE_PATH.toString()));
            fileChooser.setInitialDirectory(initFile);
            fileChooser.setInitialFileName(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.toString()));
            file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if (file != null)
                dataFilePath = file.toPath();

        }
        if (file != null) {
            applicationTemplate.getDataComponent().saveData(dataFilePath);
            ((AppUI) applicationTemplate.getUIComponent()).setSaveDisabled();
            isSaved = true;

        }


    }

    @Override
    public void handleLoadRequest() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(applicationTemplate.manager.getPropertyValue(LOAD.toString()));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.toString()),
                        applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.toString())));
        File initFile = new File(applicationTemplate.manager.getPropertyValue(DATA_RESOURCE_PATH.toString()));
        fileChooser.setInitialDirectory(initFile);
        file = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if (file != null) {
            dataFilePath = file.toPath();
            applicationTemplate.getDataComponent().loadData(dataFilePath);
            ((AppUI) applicationTemplate.getUIComponent()).setSaveDisabled();
            ((AppUI) applicationTemplate.getUIComponent()).setCurrentDataFilePath(dataFilePath);
            if (((AppUI) applicationTemplate.getUIComponent()).isToggleSelected()) {
                ((AppUI) applicationTemplate.getUIComponent()).setToggleSelected(false);
                ((AppUI) applicationTemplate.getUIComponent()).setToggleSelected(true);
            } else
                ((AppUI) applicationTemplate.getUIComponent()).setToggleSelected(true);
        }


    }

    @Override
    public void handleExitRequest() {

        if (((AppUI) applicationTemplate.getUIComponent()).getCurrentAlgorithm() != null) {
            if (!(((AppUI) applicationTemplate.getUIComponent()).getCurrentAlgorithm().isFinished())) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText(PropertyManager.getManager().getPropertyValue(RUNNING_EXIT.toString()));
                alert.setHeaderText(PropertyManager.getManager().getPropertyValue(CURRENTLY_RUNNING.toString()));
                alert.setTitle(PropertyManager.getManager().getPropertyValue(EXIT.toString()));


                Optional<ButtonType> check = alert.showAndWait();
                if (check.get() == ButtonType.OK) {
                    if (!isSaved) {
                        ConfirmationDialog confirmationDialog = (ConfirmationDialog) applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
                        confirmationDialog.show(applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.toString()), applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK.toString()));
                        if (confirmationDialog.getSelectedOption() == ConfirmationDialog.Option.CANCEL) {
                            return;
                        } else if (confirmationDialog.getSelectedOption() == ConfirmationDialog.Option.YES) {
                            ((AppUI) applicationTemplate.getUIComponent()).getTextFlow().getChildren().clear();
                            ((AppUI) applicationTemplate.getUIComponent()).setToggleSelected(false);
                            ((AppUI) applicationTemplate.getUIComponent()).setCurrentDataFilePath(null);

                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_TITLE.toString()));
                            fileChooser.getExtensionFilters().add(
                                    new FileChooser.ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.toString()),
                                            applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.toString())));
                            File initFile = new File(applicationTemplate.manager.getPropertyValue(DATA_RESOURCE_PATH.toString()));
                            fileChooser.setInitialDirectory(initFile);
                            fileChooser.setInitialFileName(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.toString()));
                            file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                            if (file != null) {
                                dataFilePath = file.toPath();
                                applicationTemplate.getDataComponent().saveData(dataFilePath);
                                ((AppUI) applicationTemplate.getUIComponent()).setSaveDisabled();
                                isSaved = true;
                            }
                        } else {
                            ((AppUI) applicationTemplate.getUIComponent()).getTextFlow().getChildren().clear();
                            ((AppUI) applicationTemplate.getUIComponent()).setToggleSelected(false);
                            ((AppUI) applicationTemplate.getUIComponent()).setCurrentDataFilePath(null);
                            Platform.exit();
                            isSaved = false;
                        }

                    } else Platform.exit();


                }
            }

        } else {
            if (!isSaved) {
                ConfirmationDialog confirmationDialog = (ConfirmationDialog) applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
                confirmationDialog.show(applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.toString()), applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK.toString()));
                if (confirmationDialog.getSelectedOption() == ConfirmationDialog.Option.CANCEL) {
                    return;
                } else if (confirmationDialog.getSelectedOption() == ConfirmationDialog.Option.YES) {
                    ((AppUI) applicationTemplate.getUIComponent()).getTextFlow().getChildren().clear();
                    ((AppUI) applicationTemplate.getUIComponent()).setToggleSelected(false);
                    ((AppUI) applicationTemplate.getUIComponent()).setCurrentDataFilePath(null);
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_TITLE.toString()));
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.toString()),
                                    applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.toString())));
                    File initFile = new File(applicationTemplate.manager.getPropertyValue(DATA_RESOURCE_PATH.toString()));
                    fileChooser.setInitialDirectory(initFile);
                    fileChooser.setInitialFileName(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.toString()));
                    file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                    if (file != null) {
                        dataFilePath = file.toPath();
                        applicationTemplate.getDataComponent().saveData(dataFilePath);
                        ((AppUI) applicationTemplate.getUIComponent()).setSaveDisabled();
                        isSaved = true;

                    }
                } else {
                    ((AppUI) applicationTemplate.getUIComponent()).getTextFlow().getChildren().clear();
                    ((AppUI) applicationTemplate.getUIComponent()).setToggleSelected(false);
                    ((AppUI) applicationTemplate.getUIComponent()).setCurrentDataFilePath(null);
                    Platform.exit();
                    isSaved = false;
                }

            } else Platform.exit();

//        if (file != null) {
//            applicationTemplate.getDataComponent().saveData(dataFilePath);
//            ((AppUI) applicationTemplate.getUIComponent()).setSaveDisabled();
//            isSaved = true;
//
//        }

        }

    }


    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        Image screenShot = ((AppUI) applicationTemplate.getUIComponent()).getChart().snapshot(new SnapshotParameters(), null);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_TITLE.toString()));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(applicationTemplate.manager.getPropertyValue(PNG_EXT_DESC.toString()),
                        applicationTemplate.manager.getPropertyValue(PNG_EXT.toString())));
        File initFile = new File(applicationTemplate.manager.getPropertyValue(DATA_RESOURCE_PATH.toString()));
        fileChooser.setInitialDirectory(initFile);
        file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if (file != null)
            ImageIO.write(SwingFXUtils.fromFXImage(screenShot, null), applicationTemplate.manager.getPropertyValue(PNG.toString()), file);
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
        {
            ConfirmationDialog confirmationDialog = (ConfirmationDialog) applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
            confirmationDialog.show(applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.toString()), applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK.toString()));
            if (confirmationDialog.getSelectedOption() == ConfirmationDialog.Option.CANCEL) {
                return false;
            } else if (confirmationDialog.getSelectedOption() == ConfirmationDialog.Option.YES) {
                ((AppUI) applicationTemplate.getUIComponent()).getTextFlow().getChildren().clear();
                ((AppUI) applicationTemplate.getUIComponent()).setToggleSelected(false);
                ((AppUI) applicationTemplate.getUIComponent()).setCurrentDataFilePath(null);
                return true;
            } else {
                ((AppUI) applicationTemplate.getUIComponent()).getTextFlow().getChildren().clear();
                ((AppUI) applicationTemplate.getUIComponent()).setToggleSelected(false);
                ((AppUI) applicationTemplate.getUIComponent()).setCurrentDataFilePath(null);
                applicationTemplate.getDataComponent().clear();
                applicationTemplate.getUIComponent().clear();
                isSaved = false;

                return false;
            }
        }
    }


}
