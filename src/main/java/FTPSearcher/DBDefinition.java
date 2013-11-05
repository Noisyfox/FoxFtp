package FTPSearcher;

/**
 * Created with IntelliJ IDEA.
 * User: Noisyfox
 * Date: 13-11-2
 * Time: 下午11:32
 * To change this template use File | Settings | File Templates.
 */
public class DBDefinition {
    public static final String CONFIG_FILE = "searcherConfig.xml";

    public static final String STATUS_FTP_ID = "id";
    public static final String STATUS_FTP_NAME = "STATUS_FTP_NAME";
    public static final String STATUS_FILE_TOTAL = "STATUS_FILE_TOTAL";
    public static final String STATUS_FILE_FILE = "STATUS_FILE_FILE";
    public static final String STATUS_FILE_DIR = "STATUS_FILE_DIR";
    public static final String STATUS_LAST_DOC_TIME = "STATUS_LAST_DOC_TIME";
    public static final String STATUS_FTP_PATH = "STATUS_FTP_PATH";
    public static final String STATUS_INDEX_PATH = "STATUS_INDEX_PATH";
    public static final String STATUS_URL_PREFIX = "STATUS_URL_PREFIX";

    public static final String LATEST_FILE_ID = "id";
    public static final String LATEST_FILE_NAME = "name";
    public static final String LATEST_UPDATE_TIME = "time";

    public static final String RECORDED_ID = "id";
    public static final String RECORDED_NAME = "name";

    public static final String CONFIG_DB_USERNAME = "DB_USERNAME";
    public static final String CONFIG_DB_PASSWD = "DB_PASSWD";
    public static final String CONFIG_DB_URL = "DB_URL";
    public static final String CONFIG_DB_NAME = "DB_NAME";
    public static final String CONFIG_DB_TABLE_FTPSTATUS = "DB_TABLE_FTPSTATUS";
    public static final String CONFIG_DB_TABLE_LATESTFILES = "DB_TABLE_LATESTFILES";
    public static final String CONFIG_DB_TABLE_RECORDEDFILES = "DB_TABLE_RECORDEDFILES";
}
