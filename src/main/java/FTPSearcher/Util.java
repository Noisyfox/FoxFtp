package FTPSearcher;

import java.io.File;

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
}
