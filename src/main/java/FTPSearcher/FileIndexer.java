package FTPSearcher;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.servlet.ServletContext;

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

public class FileIndexer {

	ServletContext _context = null;
	String _ftpPath = null;
	String _indexPath = null;

	long _fileCount = 0;
	long _dirCount = 0;

	public FileIndexer(ServletContext context) {
		_context = context;
		Properties serviceStatues = ServiceStatuesUtil
				.getServiceStatues(context);
		_ftpPath = serviceStatues.getProperty(
				ServiceStatuesUtil.STATUES_FTP_PATH, "");
		_indexPath = serviceStatues.getProperty(
				ServiceStatuesUtil.STATUES_INDEX_PATH, "");
		_ftpPath = new File(_ftpPath).getAbsolutePath();
		_indexPath = new File(_indexPath).getAbsolutePath();
	}

	public final String reIndex() {

		// 先创建备份文件
		File indexFile = new File(_indexPath);
		if (indexFile.exists()
				&& (!indexFile.isDirectory() || !indexFile.canRead() || !indexFile
						.canWrite()))
			return "指定的索引目录无法访问！";
		if (indexFile.exists()) {
			File roots[] = File.listRoots();
			boolean isRoot = false;
			for (File root : roots) {
				if (indexFile.getAbsolutePath().equals(root.getAbsolutePath())) {
					isRoot = true;
					break;
				}
			}
			if (isRoot) {
				return "指定的索引目录为根目录，无法在上一级目录中创建备份！请更改索引目录！";
			}
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

		// 开始创建索引
		String result = mkIndex();
		if (!result.isEmpty()) {
			return "索引创建失败！";
		}

		// 更新服务器状态
		ServiceStatuesUtil.saveServiceStatues(_context,
				ServiceStatuesUtil.STATUES_FILE_DIR, String.valueOf(_dirCount));
		ServiceStatuesUtil.saveServiceStatues(_context,
				ServiceStatuesUtil.STATUES_FILE_FILE,
				String.valueOf(_fileCount));
		ServiceStatuesUtil.saveServiceStatues(_context,
				ServiceStatuesUtil.STATUES_FILE_TOTAL,
				String.valueOf(_fileCount + _dirCount));

		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
		ServiceStatuesUtil.saveServiceStatues(_context,
				ServiceStatuesUtil.STATUES_LAST_DOC_TIME,
				sdf.format(new Date()));

		return "";
	}

	private final String mkIndex() {
		System.out.println("Indexing to directory '" + _indexPath + "'...");
		Directory indexDir = null;
		IndexWriter iwriter = null;
		try {
			indexDir = FSDirectory.open(new File(_indexPath));
			Analyzer analyzer = new IKAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_42,
					analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			iwc.setRAMBufferSizeMB(256.0);
			iwriter = new IndexWriter(indexDir, iwc);

			indexFiles(iwriter, new File(_ftpPath));
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		} finally {
			if (iwriter != null) {
				try {
					iwriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (indexDir != null) {
				try {
					indexDir.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return "";
	}

	private boolean addEntry(IndexWriter writer, File file) {
		Document doc = new Document();
		String fileName = file.getName();
		if (fileName.isEmpty())
			return false;

		String recPath = Util.getRelativePath(_ftpPath, file.getAbsolutePath());
		if (recPath == null)
			return false;
		if (recPath.trim().isEmpty())
			return false;

		doc.add(new TextField("fileName", fileName, Field.Store.YES));
		if (file.isDirectory()) {
			doc.add(new TextField("isDir", "true", Field.Store.YES));
		} else {
			doc.add(new TextField("isDir", "false", Field.Store.YES));
		}
		doc.add(new StringField("filePath", recPath, Field.Store.YES));

		try {
			writer.addDocument(doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		if (file.isDirectory()) {
			_dirCount++;
		} else {
			_fileCount++;
		}

		return true;
	}

	private void indexFiles(IndexWriter writer, File file) {
		if (file.canRead()) {
			//System.out.println("Add file/dir:" + file.getAbsolutePath());
			if (file.isDirectory()) {
				// 记录目录
				addEntry(writer, file);
				File[] files = file.listFiles();
				if (files != null) {
					for (File f : files) {
						indexFiles(writer, f);
					}
				}
			} else {
				addEntry(writer, file);
			}
		}
	}
}
