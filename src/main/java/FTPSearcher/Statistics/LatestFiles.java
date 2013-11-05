package FTPSearcher.Statistics;

import FTPSearcher.DBConnectionProvider;
import FTPSearcher.FileLister.FileDescription;
import FTPSearcher.Logger.InternalLogger;
import FTPSearcher.ServiceStatusUtil;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import static FTPSearcher.DBDefinition.*;

/**
 * Created with IntelliJ IDEA.
 * User: Noisyfox
 * Date: 13-11-2
 * Time: 下午9:58
 * To change this template use File | Settings | File Templates.
 */
public class LatestFiles {
    private static final int SHOW_NEW_FILE_COUNT = 10;
    private static LatestFiles mInstance = new LatestFiles();
    private static SimpleDateFormat mDataFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd");

    private final ConcurrentLinkedQueue<Editor> mEditorQueue = new ConcurrentLinkedQueue<Editor>();

    private final Object mSyncObj = new Object();
    private HashMap<String, String> mRecordedFiles = new HashMap<String, String>();
    private NewFile[] mNewFiles = new NewFile[SHOW_NEW_FILE_COUNT];
    private int mNewFilesCount = 0;
    private int mNewFilesPointer = 0;

    private String mTableName_latest;
    private String mTableName_recorded;

    private String mSql_createTable_latest;
    private String mSql_createTable_recorded;
    private String mSqlFormatter_saveLatestFiles;
    private String mSqlFormatter_addFileRecord;

    private Committer mCommitter = null;

    private LatestFiles() {
    }

    public static LatestFiles getInstance() {
        return mInstance;
    }

    public void initialize() {
        synchronized (mSyncObj) {
            mTableName_latest = ServiceStatusUtil.getProperties(CONFIG_FILE)
                    .getProperty(CONFIG_DB_TABLE_LATESTFILES, "");
            mTableName_recorded = ServiceStatusUtil.getProperties(CONFIG_FILE)
                    .getProperty(CONFIG_DB_TABLE_RECORDEDFILES, "");

            mSql_createTable_latest = String.format("CREATE TABLE IF NOT EXISTS `%s` (`%s` int NOT NULL auto_increment,`%s` text,`%s` date, PRIMARY KEY(`%s`)) ENGINE = MyISAM DEFAULT CHARSET=utf8;",
                    mTableName_latest, LATEST_FILE_ID, LATEST_FILE_NAME, LATEST_UPDATE_TIME, LATEST_FILE_ID);
            mSql_createTable_recorded = String.format("CREATE TABLE IF NOT EXISTS `%s` (`%s` int NOT NULL auto_increment,`%s` text, PRIMARY KEY(`%s`)) ENGINE = MyISAM DEFAULT CHARSET=utf8;",
                    mTableName_recorded, RECORDED_ID, RECORDED_NAME, RECORDED_ID);

            mSqlFormatter_saveLatestFiles = String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`) VALUES ",
                    mTableName_latest, LATEST_FILE_ID, LATEST_FILE_NAME, LATEST_UPDATE_TIME)
                    + "('%d', '%s', '%s');";
            mSqlFormatter_addFileRecord = String.format("INSERT INTO `%s` (%s, `%s`) VALUES ",
                    mTableName_recorded, LATEST_FILE_ID, RECORDED_NAME)
                    + "(NULL, '%s');";

            Connection conn = DBConnectionProvider.getConnection();
            if (conn != null) {
                createDBifNotExists(conn);

                //loadRecordedFiles_DB(conn);
                loadLatestFiles_DB(conn);

                try {
                    conn.close();
                } catch (SQLException e) {
                    InternalLogger.logException(e);
                }
            }
        }
    }

    public void saveLatestFiles() {
        synchronized (mSyncObj) {
            Connection conn = DBConnectionProvider.getConnection();
            if (conn != null) {
                createDBifNotExists(conn);
                saveLatestFiles_DB(conn);

                try {
                    conn.close();
                } catch (SQLException e) {
                    InternalLogger.logException(e);
                }
            }
        }
    }

    public void startCommitter() {
        if (mCommitter != null) {
            throw new IllegalStateException();
        }
        mCommitter = new Committer();
        mCommitter.start();
    }

    public void stopCommitter() {
        if (mCommitter == null) {
            throw new IllegalStateException();
        }
        mCommitter.isShutdown = true;
        synchronized (mEditorQueue) {
            mEditorQueue.clear();
            mEditorQueue.notify();
        }

        mCommitter = null;
    }

    public NewFile[] getNewFiles() {
        synchronized (mSyncObj) {
            NewFile[] files = new NewFile[mNewFilesCount];
            int k = 0;
            for (int i = mNewFilesPointer; i < mNewFilesPointer + mNewFilesCount; i++, k++) {
                files[mNewFilesCount - k - 1] = mNewFiles[i % SHOW_NEW_FILE_COUNT];
            }
            return files;
        }
    }

    public void putNewFile(String name, String date) {
        synchronized (mSyncObj) {
            if (mNewFilesCount == SHOW_NEW_FILE_COUNT) {
                mNewFiles[mNewFilesPointer].fileName = name;
                mNewFiles[mNewFilesPointer].updateTime = date;
                mNewFilesPointer++;
                mNewFilesPointer %= SHOW_NEW_FILE_COUNT;
            } else {
                mNewFilesCount++;
                int p = mNewFilesPointer + mNewFilesCount - 1;
                p %= SHOW_NEW_FILE_COUNT;
                if (mNewFiles[p] == null) {
                    mNewFiles[p] = new NewFile();
                }
                mNewFiles[p].fileName = name;
                mNewFiles[p].updateTime = date;
            }
        }
    }

    public Editor edit() {
        return new Editor();
    }

    private void createDBifNotExists(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(mSql_createTable_latest);
            statement.executeUpdate(mSql_createTable_recorded);
        } catch (SQLException e) {
            InternalLogger.logException(e);
        }

    }

    private void loadRecordedFiles_DB(Connection conn) {
        synchronized (mSyncObj) {
            try {
                if (conn == null || conn.isClosed()) {
                    return;
                }

                mRecordedFiles.clear();

                Statement statement = conn.createStatement();

                StringBuilder sb = new StringBuilder();
                sb.append("SELECT * FROM `").append(mTableName_recorded);
                String sql = sb.toString();

                ResultSet rs = statement.executeQuery(sql);
                // 获得数据结果集合
                ResultSetMetaData rmeta = rs.getMetaData();

                // 确定数据集的列数，亦字段数
                int numColumns = rmeta.getColumnCount();

                Map<String, String> result = new HashMap<String, String>();
                while (rs.next()) {
                    result.clear();
                    for (int i2 = 0; i2 < numColumns; i2++) {
                        Object obj = rs.getObject(i2 + 1);
                        result.put(rmeta.getColumnName(i2 + 1), obj == null ? null
                                : obj.toString());
                    }

                    String v = result.get(RECORDED_NAME);
                    mRecordedFiles.put(v, v);
                }

            } catch (SQLException e) {
                InternalLogger.logException(e);
            }
        }
    }

    private void loadLatestFiles_DB(Connection conn) {
        synchronized (mSyncObj) {
            try {
                if (conn == null || conn.isClosed()) {
                    return;
                }
                mNewFilesCount = 0;

                Map<String, String> result = new HashMap<String, String>();

                Statement statement = conn.createStatement();

                StringBuilder sb = new StringBuilder();
                sb.append("SELECT * FROM `").append(mTableName_latest).append("` WHERE `")
                        .append(mTableName_latest).append("`.`id` = ");
                String sql = sb.toString();

                for (int i = 1; i <= SHOW_NEW_FILE_COUNT; i++) {
                    result.clear();
                    ResultSet rs = statement.executeQuery(sql + i);

                    // 获得数据结果集合
                    ResultSetMetaData rmeta = rs.getMetaData();

                    // 确定数据集的列数，亦字段数
                    int numColumns = rmeta.getColumnCount();
                    if (rs.next()) {
                        for (int i2 = 0; i2 < numColumns; i2++) {
                            Object obj = rs.getObject(i2 + 1);
                            result.put(rmeta.getColumnName(i2 + 1), obj == null ? null
                                    : obj.toString());
                        }
                        NewFile nf = new NewFile();
                        nf.fileName = result.get(LATEST_FILE_NAME);
                        nf.updateTime = result.get(LATEST_UPDATE_TIME);
                        mNewFiles[(mNewFilesCount + mNewFilesPointer) % SHOW_NEW_FILE_COUNT] = nf;
                        mNewFilesCount++;
                    } else {
                        break;
                    }
                }

            } catch (SQLException e) {
                InternalLogger.logException(e);
            }
        }
    }

    private void saveLatestFiles_DB(Connection conn) {
        synchronized (mSyncObj) {
            try {
                if (conn == null || conn.isClosed()) {
                    return;
                }

                Statement statement = conn.createStatement();
                //先清空表
                statement.execute("TRUNCATE " + mTableName_latest);
                //写入数据
                int k = 1;
                for (int i = mNewFilesPointer; i < mNewFilesPointer + mNewFilesCount; i++, k++) {
                    NewFile nf = mNewFiles[i % SHOW_NEW_FILE_COUNT];
                    String sql = String.format(mSqlFormatter_saveLatestFiles, k, nf.fileName, nf.updateTime);
                    statement.execute(sql);
                }

            } catch (SQLException e) {
                InternalLogger.logException(e);
            }
        }
    }

    public void saveFileRecord_DB(Connection conn, List<String> fileList) {
        synchronized (mSyncObj) {
            try {
                if (conn == null || conn.isClosed()) {
                    return;
                }

                Statement statement = conn.createStatement();
                //先清空表
                statement.execute("TRUNCATE " + mTableName_recorded);
                for (String f : fileList) {
                    String sql = String.format(mSqlFormatter_addFileRecord, f);
                    statement.execute(sql);
                }
            } catch (SQLException e) {
                InternalLogger.logException(e);
            }
        }
    }

    public class NewFile {
        public String fileName;
        public String updateTime;
    }

    public class Editor {
        boolean isCommitted = false;
        protected List<String> mRecordedFilesList = new ArrayList<String>();
        protected String mTime;

        public synchronized void record(FileDescription file) {
            if (isCommitted) {
                throw new IllegalStateException();
            }
            String v = file.getFilePath();
            v = v.replace("\\", "/");
            if (file.isDirectory()) {
                v += "/";
            }
            mRecordedFilesList.add(v);
        }

        public synchronized void commit() {
            if (isCommitted) {
                throw new IllegalStateException();
            }
            isCommitted = true;

            mTime = mDataFormatter.format(new Date(System.currentTimeMillis()));

            synchronized (mEditorQueue) {
                mEditorQueue.offer(this);
                mEditorQueue.notifyAll();
                System.out.println("Commit submitted!");
            }
        }

        private void doCommit() {
            Connection conn = DBConnectionProvider.getConnection();
            if (conn != null) {
                loadRecordedFiles_DB(conn);
                saveFileRecord_DB(conn, mRecordedFilesList);
                try {
                    conn.close();
                } catch (SQLException e) {
                    InternalLogger.logException(e);
                }
            }

            List<String> newFileList = new ArrayList<String>();
            for (String s : mRecordedFilesList) {
                if (!mRecordedFiles.containsKey(s)) {
                    newFileList.add(s);
                }
            }
            int size = newFileList.size();
            if (size == 0) return;

            String[] filesArray = new String[size];
            int size2 = size;
            newFileList.toArray(filesArray);
            Arrays.sort(filesArray);
            String prefix = filesArray[0];
            for (int i = 1; i < size; i++) {
                if (filesArray[i].startsWith(prefix)) {
                    filesArray[i] = null;
                    size2--;
                } else {
                    prefix = filesArray[i];
                }
            }
            String[] filesArray2 = new String[size2];
            int p = 0;
            for (int i = 0; i < size; i++) {
                if (filesArray[i] != null) {
                    filesArray2[p] = filesArray[i];
                    p++;
                }
            }
            for (int i = 0; i < size2; i++) {
                if (filesArray2[i].endsWith("/")) { //文件夹，直接载入
                    // 获取文件夹名称
                    String name = filesArray2[i].substring(0, filesArray2[i].length() - 1);
                    name = name.substring(name.lastIndexOf("/") + 1);
                    putNewFile(name, mTime);
                } else {
                    // 获取文件名称
                    String name = filesArray2[i].substring(filesArray2[i].lastIndexOf("/") + 1);
                    putNewFile(name, mTime);
                }
            }
        }
    }

    private class Committer extends Thread {
        protected boolean isShutdown = false;

        @Override
        public void run() {
            while (!isShutdown) {
                synchronized (mEditorQueue) {
                    if (mEditorQueue.isEmpty()) {
                        try {
                            mEditorQueue.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                    if (mEditorQueue.isEmpty()) {
                        System.out.println("Committer exit!");
                        return;
                    }
                }
                Editor e = mEditorQueue.poll();
                e.doCommit();
                saveLatestFiles();
                System.out.println("Commit updated!");
            }
            System.out.println("Committer exit!");
        }
    }
}
