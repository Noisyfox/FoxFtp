package FTPSearcher;

import FTPSearcher.Logger.InternalLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

import static FTPSearcher.DBDefinition.*;
import static FTPSearcher.Util.closeConnection;
import static FTPSearcher.Util.closeStatement;

public class ServiceStatusUtil {

    public static final String CLASS_PATH = Util.pathConnect(new String[]{ServiceStatusUtil.class.getClassLoader().getResource("/").getPath()});
    public static final String PROPERTIES_DIR = Util.pathConnect(new String[]{CLASS_PATH, "properties"});

    public static final Map<String, Properties> mCachedProperties = new HashMap<String, Properties>();

    public static String getPropDir() {
        return PROPERTIES_DIR;
    }

    public static File getPropFile(String fileName) {
        return new File(Util.pathConnect(new String[]{getPropDir(),
                fileName}));
    }

    public static Properties getProperties(String fileName) {
        if (mCachedProperties.containsKey(fileName)) {
            return mCachedProperties.get(fileName);
        }

        File pFile = getPropFile(fileName);

        FileInputStream pInStream = null;

        Properties properties = new Properties();

        try {
            pInStream = new FileInputStream(pFile);

            properties.loadFromXML(pInStream);
            InternalLogger.logProperties(properties);

            mCachedProperties.put(fileName, properties);
        } catch (IOException e) {
            InternalLogger.logException(e);
        } finally {
            if (pInStream != null) {
                try {
                    pInStream.close();
                } catch (IOException e) {
                    InternalLogger.logException(e);
                }
            }
        }

        return properties;
    }

    public static void unregisterDrivers() {
        Enumeration<Driver> d = DriverManager.getDrivers();
        while (d.hasMoreElements()) {
            try {
                DriverManager.deregisterDriver(d.nextElement());
            } catch (SQLException e) {
                InternalLogger.logException(e);
            }
        }
    }

    private static void createDBifNotExists(Connection connection) {
        String table = getProperties(CONFIG_FILE)
                .getProperty(CONFIG_DB_TABLE_FTPSTATUS, "");

        if (connection == null) {
            return;
        }
        Statement statement = null;
        try {
            statement = connection.createStatement();

            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE IF NOT EXISTS `");
            sb.append(table);
            sb.append("` (`");
            sb.append(STATUS_FTP_ID);
            sb.append("` int NOT NULL auto_increment,`");
            sb.append(STATUS_FTP_NAME);
            sb.append("` text,`");
            sb.append(STATUS_FTP_PATH);
            sb.append("` text,`");
            sb.append(STATUS_INDEX_PATH);
            sb.append("` text,`");
            sb.append(STATUS_URL_PREFIX);
            sb.append("` text,`");
            sb.append(STATUS_FILE_TOTAL);
            sb.append("` bigint,`");
            sb.append(STATUS_FILE_FILE);
            sb.append("` bigint,`");
            sb.append(STATUS_FILE_DIR);
            sb.append("` bigint,`");
            sb.append(STATUS_LAST_DOC_TIME);
            sb.append("` datetime, PRIMARY KEY(`");
            sb.append(STATUS_FTP_ID);
            sb.append("`)) ENGINE = MyISAM DEFAULT CHARSET=utf8;");

            String sql = sb.toString();

            statement.executeUpdate(sql);

            // 如果表空则创建空记录
            ResultSet rs2 = statement.executeQuery("SELECT * FROM `" + table
                    + "`");
            if (!rs2.first()) {
                statement.execute("INSERT INTO `" + table + "` () VALUES ();");
            }
        } catch (SQLException e) {
            InternalLogger.logException(e);
        } finally {
            closeStatement(statement);
        }

    }

    public static boolean saveServiceStatus_DB(int id, Map<String, String> data) {
        // UPDATE `foxftp`.`ftpstatus` SET `name` = 'kkk' WHERE
        // `ftpstatus`.`id` = 1
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DBConnectionProvider.getConnection();
            if (conn == null || conn.isClosed()) {
                return false;
            }

            createDBifNotExists(conn);

            statement = conn.createStatement();
            String table = getProperties(CONFIG_FILE)
                    .getProperty(CONFIG_DB_TABLE_FTPSTATUS, "");

            Set<Entry<String, String>> dataSet = data.entrySet();
            for (Entry<String, String> entry : dataSet) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value == null)
                    continue;
                value = value.replace("\\", "\\\\");
                // statement用来执行SQL语句
                StringBuilder sb = new StringBuilder();
                sb.append("UPDATE `").append(table).append("` SET `")
                        .append(key).append("` = '").append(value)
                        .append("' WHERE `").append(table).append("`.`id` = ")
                        .append(id);
                String sql = sb.toString();
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            InternalLogger.logException(e);
            return false;
        } finally {
            closeStatement(statement);
            closeConnection(conn);
        }
        return true;
    }

    public static Map<String, String> getServiceStatus_DB(int id) {
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DBConnectionProvider.getConnection();
            if (conn == null || conn.isClosed()) {
                return null;
            }
            createDBifNotExists(conn);

            Map<String, String> result = new HashMap<String, String>();

            String table = getProperties(CONFIG_FILE)
                    .getProperty(CONFIG_DB_TABLE_FTPSTATUS, "");

            statement = conn.createStatement();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM `").append(table).append("` WHERE `")
                    .append(table).append("`.`id` = ").append(id);
            String sql = sb.toString();
            ResultSet rs = statement.executeQuery(sql);

            // 获得数据结果集合
            ResultSetMetaData rmeta = rs.getMetaData();

            // 确定数据集的列数，亦字段数
            int numColumns = rmeta.getColumnCount();
            if (rs.next()) {
                for (int i = 0; i < numColumns; i++) {
                    Object obj = rs.getObject(i + 1);
                    result.put(rmeta.getColumnName(i + 1), obj == null ? null
                            : obj.toString());
                }
            } else
                return null;

            return result;
        } catch (SQLException e) {
            InternalLogger.logException(e);
            return null;
        } finally {
            closeStatement(statement);
            closeConnection(conn);
        }
    }

    public static Properties getServiceStatus() {

        return Util.map2Properties(getServiceStatus_DB(1));
    }

    public static boolean saveServiceStatus(Properties status) {

        InternalLogger.getLogger().info("Updating Service status.");

        if (saveServiceStatus_DB(1, Util.properties2Map(status))) {
            InternalLogger.getLogger().info("Updating success!");
            return true;
        } else {
            return false;
        }
    }
}
