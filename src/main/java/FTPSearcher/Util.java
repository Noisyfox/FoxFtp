package FTPSearcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Util {
	public static final String pathConnect(String[] elements) {
		if (elements == null || elements.length == 0)
			return "";

		String path = "";
		for (String e : elements) {
			e = e.trim();
			if (e.startsWith(File.pathSeparator))
				path += e;
			else
				path += File.separator + e;
		}
		return path;
	}

	public static final boolean copy(String path, String copyPath) {
		File filePath = new File(path);
		DataInputStream read = null;
		DataOutputStream write = null;
		try {
			if (filePath.isDirectory()) {
				File[] list = filePath.listFiles();
				for (int i = 0; i < list.length; i++) {
					String newPath = path + File.separator + list[i].getName();
					String newCopyPath = copyPath + File.separator
							+ list[i].getName();
					File newFile = new File(copyPath);
					if (!newFile.exists()) {
						newFile.mkdir();
					}
					copy(newPath, newCopyPath);
				}
			} else if (filePath.isFile()) {
				read = new DataInputStream(new BufferedInputStream(
						new FileInputStream(path)));
				write = new DataOutputStream(new BufferedOutputStream(
						new FileOutputStream(copyPath)));
				byte[] buf = new byte[1024 * 512];
				int b = 0;
				while ((b = read.read(buf)) != -1) {
					write.write(buf, 0, b);
				}
				read.close();
				write.close();
			} else {
				return false;
			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (read != null)
				try {
					read.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (write != null)
				try {
					write.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return true;
	}

	// 删除文件夹
	// param folderPath 文件夹完整绝对路径
	public static boolean delFolder(String folderPath) {
		try {
			if (!delAllFile(folderPath)) { // 删除完里面所有内容
				return false;
			}
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			if (!myFilePath.delete()) { // 删除空文件夹
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// 删除指定文件夹下所有文件
	// param path 文件夹完整绝对路径
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		flag = true;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + File.pathSeparator + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + File.pathSeparator + tempList[i]);// 再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}

	public static final String getRelativePath(String prefix,
			String absolutePath) {
		if (!absolutePath.startsWith(prefix)) {
			return null;
		} else {
			absolutePath = absolutePath.substring(prefix.length());
			if (absolutePath.startsWith(File.pathSeparator)) {
				absolutePath = absolutePath.substring(File.pathSeparator
						.length());
			}
			if (absolutePath.endsWith(File.pathSeparator)) {
				absolutePath = absolutePath.substring(0, absolutePath.length()
						- File.pathSeparator.length());
			}
			return absolutePath;
		}
	}
}
