package FTPSearcher;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import javax.servlet.ServletContext;
import java.util.Date;

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

        try {
            ServletContext lServletContext =
                    (ServletContext) jobExecutionContext.getScheduler().getContext().get("servletContext");
            System.err.println("Reindex start!");
            FileIndexer fi = new FileIndexer(lServletContext);
            String result = fi.reIndex();
            System.err.println("Reindex finish! Result:" + result);
        } catch (SchedulerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
