package FTPSearcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class ServiceStatuesUtil {
    public static final String STATUES_FTP_ID = "id";
    public static final String STATUES_FTP_NAME = "STATUES_FTP_NAME";
    public static final String STATUES_FILE_TOTAL = "STATUES_FILE_TOTAL";
    public static final String STATUES_FILE_FILE = "STATUES_FILE_FILE";
    public static final String STATUES_FILE_DIR = "STATUES_FILE_DIR";
    public static final String STATUES_LAST_DOC_TIME = "STATUES_LAST_DOC_TIME";
    public static final String STATUES_FTP_PATH = "STATUES_FTP_PATH";
    public static final String STATUES_INDEX_PATH = "STATUES_INDEX_PATH";
    public static final String STATUES_URL_PREFIX = "STATUES_URL_PREFIX";

    public static final String CONFIG_DB_USERNAME = "DB_USERNAME";
    public static final String CONFIG_DB_PASSWD = "DB_PASSWD";
    public static final String CONFIG_DB_URL = "DB_URL";
    public static final String CONFIG_DB_NAME = "DB_NAME";
    public static final String CONFIG_DB_TABLE_FTPSTATUS = "DB_TABLE_FTPSTATUS";

    public static final String CLASS_PATH = Util.pathConnect(new String[]{ServiceStatuesUtil.class.getClassLoader().getResource("/").getPath()});
    public static final String PROPERTIES_DIR = Util.pathConnect(new String[]{CLASS_PATH, "properties"});

    public static String getPropDir() {
        return PROPERTIES_DIR;
    }

    public static File getPropFile(String fileName) {
        return new File(Util.pathConnect(new String[]{getPropDir(),
                fileName}));
    }

    public static Properties getProperties(String fileName) {
        File pFile = getPropFile(fileName);

        FileInputStream pInStream = null;

        Properties properties = new Properties();

        try {
            pInStream = new FileInputStream(pFile);

            properties.loadFromXML(pInStream);
            properties.list(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (pInStream != null) {
                try {
                    pInStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static void createDBifNotExists(Connection connection) {
        String table = getProperties("searcherConfig.xml")
                .getProperty(CONFIG_DB_TABLE_FTPSTATUS, "");

        if (connection == null) {
            return;
        }

        try {
            Statement statement = connection.createStatement();

            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE IF NOT EXISTS `");
            sb.append(table);
            sb.append("` (`");
            sb.append(STATUES_FTP_ID);
            sb.append("` int NOT NULL auto_increment,`");
            sb.append(STATUES_FTP_NAME);
            sb.append("` text,`");
            sb.append(STATUES_FTP_PATH);
            sb.append("` text,`");
            sb.append(STATUES_INDEX_PATH);
            sb.append("` text,`");
            sb.append(STATUES_URL_PREFIX);
            sb.append("` text,`");
            sb.append(STATUES_FILE_TOTAL);
            sb.append("` bigint,`");
            sb.append(STATUES_FILE_FILE);
            sb.append("` bigint,`");
            sb.append(STATUES_FILE_DIR);
            sb.append("` bigint,`");
            sb.append(STATUES_LAST_DOC_TIME);
            sb.append("` datetime, PRIMARY KEY(`");
            sb.append(STATUES_FTP_ID);
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static boolean saveServiceStatues_DB(int id, Map<String, String> data) {
        // UPDATE `foxftp`.`ftpstatues` SET `name` = 'kkk' WHERE
        // `ftpstatues`.`id` = 1
        Connection conn = null;
        try {
            conn = DBConnectionProvider.getConnection();
            if (conn == null || conn.isClosed()) {
                return false;
            }

            createDBifNotExists(conn);

            Statement statement = conn.createStatement();
            String table = getProperties("searcherConfig.xml")
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
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;
    }

    public static Map<String, String> getServiceStatues_DB(int id) {
        Connection conn = null;
        try {
            conn = DBConnectionProvider.getConnection();
            if (conn == null || conn.isClosed()) {
                return null;
            }
            createDBifNotExists(conn);

            Map<String, String> result = new HashMap<String, String>();

            String table = getProperties("searcherConfig.xml")
                    .getProperty(CONFIG_DB_TABLE_FTPSTATUS, "");

            Statement statement = conn.createStatement();
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
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static Properties getServiceStatues() {

        return Util.map2Properties(getServiceStatues_DB(1));
    }

    public static boolean saveServiceStatues(Properties statues) {

        System.out.println("Updating Service statues.");

        if (saveServiceStatues_DB(1, Util.properties2Map(statues))) {
            System.out.println("Updating success!");
            return true;
        } else {
            return false;
        }
    }
}
