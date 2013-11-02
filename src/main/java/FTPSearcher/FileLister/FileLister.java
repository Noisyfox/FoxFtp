package FTPSearcher.FileLister;

/**
 * Created with IntelliJ IDEA.
 * User: Noisyfox
 * Date: 13-11-2
 * Time: 下午7:44
 * To change this template use File | Settings | File Templates.
 */
public interface FileLister {
    public String getFailureMessage();

    public boolean prepare();

    public boolean next(FileDescription fileDescription);

    public boolean cleanup();
}
