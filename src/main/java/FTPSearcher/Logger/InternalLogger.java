package FTPSearcher.Logger;

import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Noisyfox
 * Date: 13-9-18
 * Time: 下午6:04
 * To change this template use File | Settings | File Templates.
 */
public final class InternalLogger {
    private static Logger mLogger = Logger.getLogger(InternalLogger.class);

    public static Logger getLogger() {
        return mLogger;
    }

    public static void logException(Exception e) {
        //StringWriter sw = new StringWriter();
        //e.printStackTrace(new PrintWriter(sw));
        //mLogger.error(sw.toString());
        mLogger.error(e.getMessage(), e);
    }

    public static void logProperties(Properties prop) {
        StringWriter sw = new StringWriter();
        prop.list(new PrintWriter(sw));
        mLogger.info(sw.toString());
    }
}
