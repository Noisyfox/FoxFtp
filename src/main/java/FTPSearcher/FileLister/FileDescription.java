package FTPSearcher.FileLister;

/**
 * Created with IntelliJ IDEA.
 * User: Noisyfox
 * Date: 13-11-2
 * Time: 下午7:38
 * To change this template use File | Settings | File Templates.
 */
public final class FileDescription {
    private boolean mIsDirectory;
    private String mFileName;
    private String mFilePath;
    private long mFileSize;

    public void setData(boolean isDirectory, String fileName, String filePath, long fileSize) {
        mIsDirectory = isDirectory;
        mFileName = fileName;
        mFilePath = filePath;
        mFileSize = fileSize;
    }

    public boolean isDirectory() {
        return mIsDirectory;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public long getFileSize() {
        return mFileSize;
    }
}
