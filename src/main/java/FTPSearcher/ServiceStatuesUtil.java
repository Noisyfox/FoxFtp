package FTPSearcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

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
	public static final String CONFIG_DB_TABLE_FTPSTATUES = "DB_TABLE_FTPSTATUES";

	public static final String PROPERTIES_DIR = Util.pathConnect(new String[] {
			"WEB-INF", "classes", "properties" });

	public static final String getPropDir(ServletContext context) {
		return context.getRealPath(PROPERTIES_DIR);
	}

	public static final File getPropFile(ServletContext context, String fileName) {
		return new File(Util.pathConnect(new String[] { getPropDir(context),
				fileName }));
	}

	public static final Properties getProperties(ServletContext context,
			String fileName) {
		File pFile = getPropFile(context, fileName);

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

	public static final Connection getDBConnection(ServletContext context) {
		Properties serviceConfig = getProperties(context, "searcherConfig.xml");

		String driver = "com.mysql.jdbc.Driver";
		String dbName = serviceConfig.getProperty(CONFIG_DB_NAME, "");
		// String tableName = serviceConfig.getProperty(
		// CONFIG_DB_TABLE_FTPSTATUES, "");

		String url = "jdbc:mysql://"
				+ serviceConfig.getProperty(CONFIG_DB_URL, "") + "/" + dbName;
		// 数据库用户名
		String userName = serviceConfig.getProperty(CONFIG_DB_USERNAME, "");
		// 密码
		String userPasswd = serviceConfig.getProperty(CONFIG_DB_PASSWD, "");
		try {
			Class.forName(driver).newInstance();
			Connection conn = DriverManager.getConnection(url, userName,
					userPasswd);
			if (!conn.isClosed()) {
				System.out.println("Succeeded connecting to the Database!");
				return conn;
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static final void createDBifNotExists(ServletContext context,
			Connection connection) {
		String table = getProperties(context, "searcherConfig.xml")
				.getProperty(CONFIG_DB_TABLE_FTPSTATUES, "");

		if (connection == null) {
			return;
		}

		try {
			Statement statement = connection.createStatement();

			StringBuffer sb = new StringBuffer();
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

			int rs = statement.executeUpdate(sql);

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

	public static final boolean saveServiceStatues_DB(ServletContext context,
			int id, Map<String, String> data) {
		// UPDATE `foxftp`.`ftpstatues` SET `name` = 'kkk' WHERE
		// `ftpstatues`.`id` = 1
		try {
			Connection conn = getDBConnection(context);
			if (conn == null) {
				return false;
			}

			createDBifNotExists(context, conn);

			Statement statement = conn.createStatement();
			String table = getProperties(context, "searcherConfig.xml")
					.getProperty(CONFIG_DB_TABLE_FTPSTATUES, "");

			Set<Entry<String, String>> dataSet = data.entrySet();
			for (Entry<String, String> entry : dataSet) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (value == null)
					continue;
				value = value.replace("\\", "\\\\");
				// statement用来执行SQL语句
				StringBuffer sb = new StringBuffer();
				sb.append("UPDATE `").append(table).append("` SET `")
						.append(key).append("` = '").append(value)
						.append("' WHERE `").append(table).append("`.`id` = ")
						.append(id);
				String sql = sb.toString();
				int rs = statement.executeUpdate(sql);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static final Map<String, String> getServiceStatues_DB(
			ServletContext context, int id) {
		try {
			Connection conn = getDBConnection(context);
			if (conn == null) {
				return null;
			}
			createDBifNotExists(context, conn);

			Map<String, String> result = new HashMap<String, String>();

			String table = getProperties(context, "searcherConfig.xml")
					.getProperty(CONFIG_DB_TABLE_FTPSTATUES, "");

			Statement statement = conn.createStatement();
			StringBuffer sb = new StringBuffer();
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
		}
	}

	public static final Properties getServiceStatues(ServletContext context) {

		return Util.map2Properties(getServiceStatues_DB(context, 1));
	}

	public static final boolean saveServiceStatues(ServletContext context,
			Properties statues) {

		System.out.println("Updating Service statues.");

		if (saveServiceStatues_DB(context, 1, Util.properties2Map(statues))) {
			System.out.println("Updating success!");
			return true;
		} else {
			return false;
		}
	}
}
