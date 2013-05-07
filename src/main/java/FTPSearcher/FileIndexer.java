package FTPSearcher;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContext;

public class FileIndexer {
	public static final String reIndex(ServletContext context) {
		Properties serviceStatues = ServiceStatuesUtil
				.getServiceStatues(context);
		String ftpPath = serviceStatues.getProperty(
				ServiceStatuesUtil.STATUES_FTP_PATH, "");
		String indexPath = serviceStatues.getProperty(
				ServiceStatuesUtil.STATUES_INDEX_PATH, "");
		// 先创建备份文件
		File indexFile = new File(indexPath);
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
			String bkpIndex = indexPath;
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
			Util.copy(indexPath, bkpIndex);
		}

		return "";
	}
}
