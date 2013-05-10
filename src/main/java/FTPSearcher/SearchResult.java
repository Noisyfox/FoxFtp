package FTPSearcher;

import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.search.ScoreDoc;

public class SearchResult {
	public static final int CACHED_RESULT_COUNT = 200;

	public long totalMillisecond = 0L;

	public int totalResults = 0;
	public int totalPages = 0;
	public int currentPage = 0;
	public int firstHitNum = 0;

	public ScoreDoc middleResult = null;
	public ScoreDoc lastResult = null;
	public int offset_hitnum = 0;
	public int firstResult_hitnum = 0;
	public int lastResult_hitnum = 0;
	public List<ResultDocument> documents_forward = new LinkedList<ResultDocument>();
	public List<ResultDocument> documents_afterward = new LinkedList<ResultDocument>();

	public SearchRequest currentRequest = null;

	public SearchResult(SearchRequest currRequest) {
		if (currRequest == null) {
			throw new NullPointerException();
		}
		currentRequest = currRequest;
	}

}
