package FTPSearcher;

public class SearchRequest {

	public static final int REQUEST_SEARCHTYPE_NEW = 1;
	public static final int REQUEST_SEARCHTYPE_CONTINUE = 2;

	public static final int REQUEST_FILETYPE_ALL = 1;
	public static final int REQUEST_FILETYPE_FILE = 2;
	public static final int REQUEST_FILETYPE_DIR = 3;

	protected int searchType = REQUEST_SEARCHTYPE_NEW;
	protected int fileType = REQUEST_FILETYPE_ALL;
	protected String keyword = "";
	protected int jumpToPage = 0;

	public SearchRequest() {

	}

	public SearchRequest(SearchRequest lstRequest) {
		if (lstRequest == null)
			return;
		searchType = lstRequest.searchType;
		fileType = lstRequest.fileType;
		keyword = lstRequest.keyword;
		jumpToPage = lstRequest.jumpToPage;
	}

}
