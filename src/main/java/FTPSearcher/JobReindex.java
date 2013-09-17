package FTPSearcher;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Noisyfox
 * Date: 13-9-17
 * Time: 上午10:56
 * To change this template use File | Settings | File Templates.
 */
public class JobReindex  implements Job{

    public JobReindex(){
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("JobReindex!" + new Date());
    }
}
