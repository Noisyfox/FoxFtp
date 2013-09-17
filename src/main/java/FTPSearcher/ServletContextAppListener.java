package FTPSearcher; /**
 * Created with IntelliJ IDEA.
 * User: Noisyfox
 * Date: 13-9-17
 * Time: 下午6:06
 * To change this template use File | Settings | File Templates.
 */

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
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.getContext().put("servletContext", sce.getServletContext());
            scheduler.start();
        } catch (SchedulerException pSchedulerException) {
            pSchedulerException.printStackTrace();
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        try {
            scheduler.shutdown();
        } catch (SchedulerException pSchedulerException) {
            pSchedulerException.printStackTrace();
        }
    }

}
