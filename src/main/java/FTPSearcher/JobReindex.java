package FTPSearcher;

import FTPSearcher.Logger.InternalLogger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: Noisyfox
 * Date: 13-9-17
 * Time: 上午10:56
 * To change this template use File | Settings | File Templates.
 */
public class JobReindex implements Job {

    public JobReindex() {
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //try {
        //ServletContext lServletContext =
        //        (ServletContext) jobExecutionContext.getScheduler().getContext().get("servletContext");
        InternalLogger.getLogger().info("Reindex start!");
        //InternalLogger.getLogger().info(this.hashCode());
        FileIndexer fi = new FileIndexer();
        String result = fi.reIndex();
        InternalLogger.getLogger().info("Reindex finish! Result:" + result);
        //} catch (SchedulerException e) {
        //    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        //}

    }
}
