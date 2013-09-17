package FTPSearcher;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Noisyfox
 * Date: 13-9-17
 * Time: 下午8:40
 * To change this template use File | Settings | File Templates.
 */
public class DBConnectionProvider {

    public static DataSource ds;

    static {
        Properties serviceConfig = ServiceStatuesUtil.getProperties("searcherConfig.xml");

        String driver = "com.mysql.jdbc.Driver";
        String dbName = serviceConfig.getProperty(ServiceStatuesUtil.CONFIG_DB_NAME, "");

        String url = "jdbc:mysql://"
                + serviceConfig.getProperty(ServiceStatuesUtil.CONFIG_DB_URL, "") + "/" + dbName;
        // 数据库用户名
        String userName = serviceConfig.getProperty(ServiceStatuesUtil.CONFIG_DB_USERNAME, "");
        // 密码
        String userPasswd = serviceConfig.getProperty(ServiceStatuesUtil.CONFIG_DB_PASSWD, "");

        DriverAdapterCPDS cpds = new DriverAdapterCPDS();
        try {
            cpds.setDriver(driver);
        } catch (ClassNotFoundException e) {
            String msg = "Could not find driver in the classpath ";
            System.out.println(msg);
            throw new RuntimeException(msg);
        }
        cpds.setUrl(url);
        cpds.setUser(userName);
        cpds.setPassword(userPasswd);

        SharedPoolDataSource tds = new SharedPoolDataSource();
        tds.setConnectionPoolDataSource(cpds);
        tds.setMaxActive(20);
        tds.setMaxWait(50);

        ds = tds;
    }

    public static Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

}
