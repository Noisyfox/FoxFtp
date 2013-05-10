package FTPSearcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletContext;

public class ServiceStatuesUtil {
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

	public static final void readSql(ServletContext context) {

		try {
			Connection conn = getDBConnection(context);
			if (conn == null) {
				return;
			}
			// statement用来执行SQL语句
			Statement statement = conn.createStatement();
			String sql = "SELECT * FROM "
					+ getProperties(context, "searcherConfig.xml").getProperty(
							CONFIG_DB_TABLE_FTPSTATUES, "");
			ResultSet rs = statement.executeQuery(sql);

			// 获得数据结果集合
			ResultSetMetaData rmeta = rs.getMetaData();

			// 确定数据集的列数，亦字段数
			int numColumns = rmeta.getColumnCount();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static final boolean saveServiceStatues_DB(ServletContext context,
			int id, String key, String value) {
		// UPDATE `foxftp`.`ftpstatues` SET `name` = 'kkk' WHERE
		// `ftpstatues`.`id` = 1
		try {
			Connection conn = getDBConnection(context);
			if (conn == null) {
				return false;
			}
			
			value=value.replace("\\", "\\\\");
			// statement用来执行SQL语句
			Statement statement = conn.createStatement();
			String table = getProperties(context, "searcherConfig.xml")
					.getProperty(CONFIG_DB_TABLE_FTPSTATUES, "");
			String sql = "UPDATE `" + table + "` SET `" + key + "` = '" + value
					+ "' WHERE `" + table + "`.`id` = " + id;
			int rs = statement.executeUpdate(sql);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public static final Properties getServiceStatues(ServletContext context) {
		readSql(context);
		//saveServiceStatues_DB(context, 0, null, null);
		return getProperties(context, "searcherStatues.xml");
	}

	public static final boolean saveServiceStatues(ServletContext context,
			String key, String value) {
		saveServiceStatues_DB(context, 1, key, value);
		File pFile = getPropFile(context, "searcherStatues.xml");

		FileInputStream pInStream = null;
		FileOutputStream pOutStream = null;
		Properties serviceStatues = new Properties();
		System.out.println("Updating Service statues.");

		try {
			pInStream = new FileInputStream(pFile);
			serviceStatues.loadFromXML(pInStream);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
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

		System.out.println("Origin Service statues:");
		serviceStatues.list(System.out);
		if (serviceStatues.containsKey(key)) {
			serviceStatues.put(key, value);
			System.out.println("New Service statues:");
			serviceStatues.list(System.out);

		}

		System.out.println("Saving...");
		try {
			pOutStream = new FileOutputStream(pFile);
			serviceStatues.storeToXML(pOutStream, "test");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			if (pOutStream != null) {
				try {
					pOutStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("Updating success!");

		return true;
	}
}
