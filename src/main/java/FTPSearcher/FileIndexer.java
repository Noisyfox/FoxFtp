package FTPSearcher;

import FTPSearcher.FileLister.FileDescription;
import FTPSearcher.FileLister.FileLister;
import FTPSearcher.Logger.InternalLogger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class FileIndexer {

    private static final Object mSyncObj = new Object();

    public static final String FIELD_FILENAME = "fileName";
    public static final String FIELD_FILESIZE = "fileSize";
    public static final String FIELD_ISDIR = "isDir";
    public static final String FIELD_PATH = "filePath";

    String _ftpPath = null;
    String _indexPath = null;

    long _fileCount = 0;
    long _dirCount = 0;

    public FileIndexer() {
        Properties serviceStatus = ServiceStatusUtil
                .getServiceStatus();
        _ftpPath = serviceStatus.getProperty(
                ServiceStatusUtil.STATUS_FTP_PATH, "");
        _indexPath = serviceStatus.getProperty(
                ServiceStatusUtil.STATUS_INDEX_PATH, "");
        _ftpPath = new File(_ftpPath).getAbsolutePath();
        _indexPath = new File(_indexPath).getAbsolutePath();
    }

    public final String reIndex() {
        synchronized (mSyncObj) {
            // 先创建备份文件
            File indexFile = new File(_indexPath);
            File compFlag = new File(indexFile, "_indexing");// 正在建立索引的标志
            if (indexFile.exists()
                    && (!indexFile.isDirectory() || !indexFile.canRead() || !indexFile
                    .canWrite()))
                return "指定的索引目录无法访问！";
            if (indexFile.exists()) {
                File roots[] = File.listRoots();
                boolean isRoot = false;
                for (File root : roots) {
                    if (indexFile.equals(root)) {
                        isRoot = true;
                        break;
                    }
                }
                if (isRoot) {
                    return "指定的索引目录为根目录，无法在上一级目录中创建备份！请更改索引目录！";
                }
                // 先验证原索引是否完整，通过检验目录中是否存在"_indexing"文件
                // 如果原索引不完整则不进行备份
                if (!compFlag.exists()) {
                    // 检验是否存在备份文件
                    String bkpIndex = _indexPath;
                    if (bkpIndex.endsWith(File.pathSeparator)) {
                        int i = bkpIndex.lastIndexOf(File.pathSeparator);
                        bkpIndex = bkpIndex.substring(0, i - 1);
                    }
                    bkpIndex += ".backup";
                    File bkpFile = new File(bkpIndex);
                    if (bkpFile.exists()) {
                        // 删除备份文件
                        if (!Util.delFolder(bkpIndex)) {
                            return "删除备份文件失败！目录：" + bkpIndex;
                        }
                    }
                    // 备份
                    bkpFile.mkdirs();
                    Util.copy(_indexPath, bkpIndex);
                } else {
                    InternalLogger.getLogger().info("Incomplete index found!");
                }
            }

            if (!indexFile.exists()) {
                // 创建索引目录
                if (!indexFile.mkdirs()) {
                    return "创建索引目录失败！目录：" + _indexPath;
                }
            }

            // 验证ftp目录
            File ftpFile = new File(_ftpPath);
            if (!ftpFile.exists() || !ftpFile.isDirectory()) {
                return "Ftp目录不存在或指定路径是一个文件！目录：" + _ftpPath;
            }
            if (!ftpFile.canRead()) {
                return "Ftp目录不可读！目录：" + _ftpPath;
            }

            //生成索引重建标记
            if (!compFlag.exists()) {
                try {
                    compFlag.createNewFile();
                } catch (IOException e) {
                    InternalLogger.logException(e);
                    return "索引创建失败！无法建立\"_indexing\"标记！";
                }
            }
            // 开始创建索引
            String result = mkIndex();
            if (!result.isEmpty()) {
                return "索引创建失败！";
            }

            // 更新服务器状态
            Properties currentProp = ServiceStatusUtil.getServiceStatus();

            currentProp.setProperty(ServiceStatusUtil.STATUS_FILE_DIR,
                    String.valueOf(_dirCount));
            currentProp.setProperty(ServiceStatusUtil.STATUS_FILE_FILE,
                    String.valueOf(_fileCount));
            currentProp.setProperty(ServiceStatusUtil.STATUS_FILE_TOTAL,
                    String.valueOf(_fileCount + _dirCount));

            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            currentProp.setProperty(ServiceStatusUtil.STATUS_LAST_DOC_TIME,
                    sdf.format(new Date()));

            if (!ServiceStatusUtil.saveServiceStatus(currentProp)) {
                return "更新服务器状态失败！";
            }
            //清除索引重建标志
            compFlag.delete();

            return "";
        }
    }

    private String mkIndex() {
        InternalLogger.getLogger().info("Indexing to directory '" + _indexPath + "'...");
        Directory indexDir = null;
        IndexWriter iwriter = null;

        FileLister fileLister = new PythonFileLister(Util.pathConnect(new String[]{
                ServiceStatusUtil.CLASS_PATH, "lsAllfiles.py"}), _ftpPath, _indexPath, "fileList");

        FileDescription fileDescription = new FileDescription();

        try {
            indexDir = FSDirectory.open(new File(_indexPath));
            Analyzer analyzer = new IKAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_42,
                    analyzer);
            iwc.setOpenMode(OpenMode.CREATE);
            iwc.setRAMBufferSizeMB(256.0);
            iwriter = new IndexWriter(indexDir, iwc);

            if (!fileLister.prepare()) {
                return fileLister.getFailureMessage();
            }
            while (fileLister.next(fileDescription)) {
                addEntry(iwriter, fileDescription);
            }

            iwriter.forceMerge(1);

        } catch (IOException e) {
            InternalLogger.logException(e);
            return e.getMessage();
        } finally {
            if (iwriter != null) {
                try {
                    iwriter.close();
                } catch (IOException e) {
                    InternalLogger.logException(e);
                }
            }
            if (indexDir != null) {
                try {
                    indexDir.close();
                } catch (IOException e) {
                    InternalLogger.logException(e);
                }
            }
            fileLister.cleanup();
        }
        return "";
    }

    private boolean addEntry(IndexWriter writer, FileDescription fileDescription) {
        Document doc = new Document();
        // parse row and create a document
        boolean isDir = fileDescription.isDirectory();
        String filePath = fileDescription.getFilePath();
        String fileName = fileDescription.getFileName();
        String fileSize = String.valueOf(fileDescription.getFileSize());

        String recPath = Util.getRelativePath(_ftpPath,
                new File(filePath).getAbsolutePath());
        if (recPath == null)
            return false;
        if (recPath.trim().isEmpty())
            return false;

        doc.add(new TextField(FIELD_FILENAME, fileName, Field.Store.YES));
        if (isDir) {
            doc.add(new StringField(FIELD_FILESIZE, "0", Field.Store.YES));
            doc.add(new TextField(FIELD_ISDIR, "true", Field.Store.YES));
        } else {
            doc.add(new StringField(FIELD_FILESIZE, fileSize, Field.Store.YES));
            doc.add(new TextField(FIELD_ISDIR, "false", Field.Store.YES));
        }
        doc.add(new StringField(FIELD_PATH, recPath, Field.Store.YES));

        try {
            writer.addDocument(doc);
        } catch (IOException e) {
            InternalLogger.logException(e);
            return false;
        }

        if (isDir) {
            _dirCount++;
        } else {
            _fileCount++;
        }

        return true;
    }

}
