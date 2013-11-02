package FTPSearcher;

import FTPSearcher.FileLister.FileDescription;
import FTPSearcher.FileLister.FileLister;
import FTPSearcher.Logger.InternalLogger;
import org.apache.lucene.util.IOUtils;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: Noisyfox
 * Date: 13-11-2
 * Time: 下午8:55
 * To change this template use File | Settings | File Templates.
 */
public class PythonFileLister implements FileLister {

    private final String mPyPath;
    private final String mDataPath;
    private final String mList2Path;
    private final String mList2File;
    private final String mList2FilePath;

    private InputStream mDataIn;
    private BufferedReader mBufferedReader;
    private String mFailureMessage;

    public PythonFileLister(String pyPath, String dataPath, String list2Path, String list2File) {
        mPyPath = pyPath;
        mDataPath = dataPath;
        mList2Path = list2Path;
        mList2File = list2File;
        mList2FilePath = Util.pathConnect(new String[]{
                mList2Path, mList2File});
    }

    @Override
    public String getFailureMessage() {
        return mFailureMessage;
    }

    @Override
    public boolean prepare() {
        try {
            // 准备调用python
            Process process = Runtime.getRuntime().exec(
                    new String[]{"python", mPyPath, mDataPath, mList2Path,
                            mList2File});
            process.waitFor();
            // 打开临时文件
            mDataIn = new FileInputStream(mList2FilePath);
            mBufferedReader = new BufferedReader(new InputStreamReader(mDataIn,
                    IOUtils.CHARSET_UTF_8));
        } catch (Exception e) {
            mFailureMessage = e.getMessage();
            InternalLogger.logException(e);
            return false;
        }

        return true;
    }

    @Override
    public boolean next(FileDescription fileDescription) {
        try {
            String line = mBufferedReader.readLine();
            if (line != null) {
                StringTokenizer st = new StringTokenizer(line, "\t");
                String filePath = st.nextToken();
                String fileName = st.nextToken();
                String fileSize = st.nextToken();
                fileDescription.setData(fileSize.equals("-1"), fileName, filePath, Long.parseLong(fileSize));
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            mFailureMessage = e.getMessage();
            InternalLogger.logException(e);
            return false;
        }
    }

    @Override
    public boolean cleanup() {

        if (mBufferedReader != null) {
            try {
                mBufferedReader.close();
            } catch (IOException e) {
                InternalLogger.logException(e);
            }
        }
        if (mDataIn != null) {
            try {
                mDataIn.close();
            } catch (IOException e) {
                InternalLogger.logException(e);
            }
        }
        // 删除临时文件
        new File(mList2FilePath).delete();

        return true;
    }
}
