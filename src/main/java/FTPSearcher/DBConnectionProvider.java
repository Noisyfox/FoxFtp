package FTPSearcher;

import FTPSearcher.Logger.InternalLogger;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static FTPSearcher.DBDefinition.*;

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
        Properties serviceConfig = ServiceStatusUtil.getProperties(CONFIG_FILE);

        String driver = "com.mysql.jdbc.Driver";
        String dbName = serviceConfig.getProperty(CONFIG_DB_NAME, "");

        String url = "jdbc:mysql://"
                + serviceConfig.getProperty(CONFIG_DB_URL, "") + "/" + dbName
                + "?useUnicode=true&characterEncoding=utf8";
        // 数据库用户名
        String userName = serviceConfig.getProperty(CONFIG_DB_USERNAME, "");
        // 密码
        String userPasswd = serviceConfig.getProperty(CONFIG_DB_PASSWD, "");

        ComboPooledDataSource cpds = new ComboPooledDataSource();
        cpds.setUser(userName);
        cpds.setPassword(userPasswd);
        cpds.setJdbcUrl(url);
        try {
            cpds.setDriverClass(driver);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        cpds.setInitialPoolSize(30);
        cpds.setMaxIdleTime(20);
        cpds.setMaxPoolSize(100);
        cpds.setMinPoolSize(10);
        cpds.setIdleConnectionTestPeriod(60);
        cpds.setBreakAfterAcquireFailure(false);
        cpds.setAcquireRetryAttempts(10);
        cpds.setAcquireRetryDelay(1000);

        ds = cpds;
    }

    public static Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            InternalLogger.logException(e);
            return null;
        }
    }

}
