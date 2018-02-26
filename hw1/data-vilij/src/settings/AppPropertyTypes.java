package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,

    /* user interface icon file names */
    SCREENSHOT_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,
    SAVE_IOEXCEPTION,
    WRONG_DATA_FORMAT_ERROR,
    WRONG_DATA_FORMAT_ERROR_CONTENT,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,
    /* component names */
    TEXT_AREA_TITLE,
    DISPLAY,
    SAVE_TITLE,
    SEPARATOR,
    ERROR_POSITION,
    WRONG_EXTENSION,
    WRONG_EXTENSION_CONTENT,
    DUPLICATE_NAME,
    AT,
    EMPTY,
    SAVE_WRONG_DATA,
    READ_ONLY,
    LOAD,
    NAME_ERROR_MSG,
    PNG_EXT,
    PNG_EXT_DESC,
    DATA_EXCEEDED,
    PNG,
    DATA_VISUALIZATION,
    AVG,
    XPOS,
    YPOS




}
