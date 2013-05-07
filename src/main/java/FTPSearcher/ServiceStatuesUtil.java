package FTPSearcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

public class ServiceStatuesUtil {
	public static final String STATUES_FILE_TOTAL = "STATUES_FILE_TOTAL";
	public static final String STATUES_FILE_FILE = "STATUES_FILE_FILE";
	public static final String STATUES_FILE_DIR = "STATUES_FILE_DIR";
	public static final String STATUES_LAST_DOC_TIME = "STATUES_LAST_DOC_TIME";
	public static final String STATUES_FTP_PATH = "STATUES_FTP_PATH";
	public static final String STATUES_INDEX_PATH = "STATUES_INDEX_PATH";

	public static final String PROPERTIES_DIR = Util.pathConnect(new String[] {
			"WEB-INF", "classes", "properties" });

	public static final String getPropDir(ServletContext context) {
		return context.getRealPath(PROPERTIES_DIR);
	}

	public static final File getPropFile(ServletContext context, String fileName) {
		return new File(Util.pathConnect(new String[] { getPropDir(context),
				fileName }));
	}

	public static final Properties getServiceStatues(ServletContext context) {
		File pFile = getPropFile(context, "searcherStatues.xml");

		FileInputStream pInStream = null;

		Properties serviceStatues = new Properties();

		try {
			pInStream = new FileInputStream(pFile);

			serviceStatues.loadFromXML(pInStream);
			serviceStatues.list(System.out);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pInStream != null) {
				try {
					pInStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return serviceStatues;
	}

	public static final boolean saveServiceStatues(ServletContext context,
			String key, String value) {
		File pFile = getPropFile(context, "searcherStatues.xml");

		FileInputStream pInStream = null;
		FileOutputStream pOutStream = null;
		Properties serviceStatues = new Properties();
		System.out.println("Updating Service statues.");

		try {
			pInStream = new FileInputStream(pFile);
			serviceStatues.loadFromXML(pInStream);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (pInStream != null) {
				try {
					pInStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		System.out.println("Origin Service statues:");
		serviceStatues.list(System.out);
		if (serviceStatues.containsKey(key)) {
			serviceStatues.put(key, value);
			System.out.println("New Service statues:");
			serviceStatues.list(System.out);

		}

		System.out.println("Saving...");
		try {
			pOutStream = new FileOutputStream(pFile);
			serviceStatues.storeToXML(pOutStream, "test");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			if (pOutStream != null) {
				try {
					pOutStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("Updating success!");

		return true;
	}
}
