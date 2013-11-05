package FTPSearcher; /**
 * Created with IntelliJ IDEA.
 * User: Noisyfox
 * Date: 13-9-17
 * Time: 下午6:06
 * To change this template use File | Settings | File Templates.
 */

import FTPSearcher.Logger.InternalLogger;
import FTPSearcher.Statistics.LatestFiles;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener()
public class ServletContextAppListener implements ServletContextListener {
    Scheduler scheduler = null;

    // Public constructor is required by servlet spec
    public ServletContextAppListener() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
        try {
            InternalLogger.getLogger().info("Scheduler start!");
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.getContext().put("servletContext", sce.getServletContext());
            scheduler.start();
        } catch (SchedulerException e) {
            InternalLogger.logException(e);
        }

        LatestFiles.getInstance().initialize();
        LatestFiles.getInstance().startCommitter();
    }

    public void contextDestroyed(ServletContextEvent sce) {
        try {
            InternalLogger.getLogger().info("Scheduler shutdown!");
            scheduler.shutdown();
        } catch (SchedulerException e) {
            InternalLogger.logException(e);
        }
        LatestFiles.getInstance().saveLatestFiles();
        LatestFiles.getInstance().stopCommitter();
    }

}
