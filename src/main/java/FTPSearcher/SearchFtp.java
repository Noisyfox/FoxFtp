package FTPSearcher;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * Servlet implementation class SearchFtp
 */
@WebServlet("/SearchFtp")
public class SearchFtp extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static final int HIT_PER_PAGE = 40;

    final Object indexPrepareSync = new Object();
    File indexFile = null;
    Directory indexDir = null;
    IndexReader indexReader = null;
    IndexSearcher indexSearcher = null;
    Analyzer analyzer = new IKAnalyzer();
    QueryParser fileNameParser = new QueryParser(Version.LUCENE_42,
            FileIndexer.FIELD_FILENAME, analyzer);
    QueryParser isDirParser = new QueryParser(Version.LUCENE_42,
            FileIndexer.FIELD_ISDIR, analyzer);

    String url_prefix = null;

    private void doCleanUp() {
        synchronized (indexPrepareSync) {
            if (indexReader != null) {
                try {
                    indexReader.close();
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

            indexFile = null;
            indexDir = null;
            indexReader = null;
            indexSearcher = null;
        }
    }

    private boolean getReadyForSearch() {
        synchronized (indexPrepareSync) {
            if (indexSearcher == null) {
                doCleanUp();
            }

            Properties sp = ServiceStatusUtil
                    .getServiceStatus();
            url_prefix = sp.getProperty(ServiceStatusUtil.STATUS_URL_PREFIX,
                    "").trim();

            String indexPath = sp.getProperty(
                    ServiceStatusUtil.STATUS_INDEX_PATH, "").trim();

            if (indexPath.isEmpty()) {
                return false;
            }

            File _indexFile = new File(indexPath);
            if (_indexFile.equals(indexFile)) {
                // 已经打开过当前目录了，检测是否发生变化，此时必然存在旧的indexSearcher，
                // 否则会执行doCleanUp，那么这个条件就不会成立。
                try {
                    DirectoryReader _dr = DirectoryReader
                            .openIfChanged((DirectoryReader) indexReader);
                    if (_dr == null) {
                        return true;
                    }
                    // 重新创建Searcher
                    indexSearcher = new IndexSearcher(_dr);
                    return true;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    doCleanUp();
                }
            }

            indexFile = _indexFile;
            if (!indexFile.canRead() || !indexFile.isDirectory()) {
                return false;
            }

            try {
                indexDir = FSDirectory.open(indexFile);
                indexReader = DirectoryReader.open(indexDir);
                indexSearcher = new IndexSearcher(indexReader);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    @Override
    public void destroy() {
        System.out.println("onDestroy");
        doCleanUp();
        ServiceStatusUtil.unregisterDrivers();
        System.gc();
        super.destroy();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        getReadyForSearch();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("ISO-8859-1");// 汉字转码
        //  设定内容类型为HTML网页UTF-8编码 
        response.setContentType("text/html;charset=UTF-8");//  输出页面
        // PrintWriter out = response.getWriter();
        // out.println("<html><head>");
        // out.println("<title>First Servlet Hello</title>");
        // out.println("</head><body>");
        // out.println("Hello world my first Servlet!");
        // out.println("</body></html>");
        // out.close();

        // Take all completed form fields and add to a Properties object
        Properties completedFormFields = new Properties();
        Enumeration<?> pNames = request.getParameterNames();
        while (pNames.hasMoreElements()) {
            String propName = (String) pNames.nextElement();
            String value = request.getParameter(propName);
            if ((value != null) && (value.trim().length() > 0)) {
                completedFormFields.setProperty(propName, value);
            }
        }

        String inputStr = completedFormFields.getProperty("keyword", "").trim();
        // String str = "";
        // if (!inputStr.isEmpty()) {
        // str = "喵呜~你刚刚输入的文字是：" + inputStr;
        // }
        // request.setAttribute("results", str);

        // 获取请求目标页数
        int targetPage = 1;
        try {
            String _targetPage = completedFormFields.getProperty("page", "")
                    .trim();
            if (!_targetPage.isEmpty()) {
                targetPage = Integer.valueOf(_targetPage);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (targetPage <= 0) {
            targetPage = 1;
        }

        int targetFileType;
        // 获取请求文件类型
        String _fileType = completedFormFields.getProperty("fileType", "all")
                .trim().toLowerCase();
        if (_fileType.equals("all")) {
            targetFileType = SearchRequest.REQUEST_FILETYPE_ALL;
        } else if (_fileType.equals("file")) {
            targetFileType = SearchRequest.REQUEST_FILETYPE_FILE;
        } else if (_fileType.equals("dir")) {
            targetFileType = SearchRequest.REQUEST_FILETYPE_DIR;
        } else {
            targetFileType = SearchRequest.REQUEST_FILETYPE_ALL;
        }

        // 检查浏览器session
        HttpSession currentSession = request.getSession();

        SearchResult lastResult = (SearchResult) currentSession
                .getAttribute("savedResult");
        SearchRequest nowRequest;
        if (lastResult != null) {
            currentSession.removeAttribute("searchResult");
            // 判断是否是同一个搜索内容
            if (inputStr.equals(lastResult.currentRequest.keyword)
                    && lastResult.currentRequest.fileType == targetFileType
                    // 如果两次重复搜索同样的页面则认为需要重新开始搜索
                    && lastResult.currentRequest.jumpToPage != targetPage
                    // 必须前一次已经有结果
                    && lastResult.totalResults != 0) {

                // 继续上一次的搜索
                nowRequest = new SearchRequest(lastResult.currentRequest);
                nowRequest.searchType = SearchRequest.REQUEST_SEARCHTYPE_CONTINUE;

            } else {
                nowRequest = new SearchRequest();
                nowRequest.searchType = SearchRequest.REQUEST_SEARCHTYPE_NEW;
            }
        } else {
            nowRequest = new SearchRequest();
            nowRequest.searchType = SearchRequest.REQUEST_SEARCHTYPE_NEW;
        }

        nowRequest.keyword = inputStr;
        nowRequest.jumpToPage = targetPage;
        nowRequest.fileType = targetFileType;

        // 开始搜索
        Date start = new Date();
        SearchResult searchResult = searchFile(lastResult, nowRequest);
        Date end = new Date();
        if (searchResult != null) {
            searchResult.totalMillisecond = end.getTime() - start.getTime();
            currentSession.setAttribute("savedResult", searchResult);
            request.setAttribute("searchResult", searchResult);
            request.setAttribute("searchMessage",
                    Util.genJSON(true, "Search success!"));
        } else {
            request.setAttribute("searchMessage",
                    Util.genJSON(false, "Searcher not prepared!"));
        }

        RequestDispatcher dispatcher = getServletContext()
                .getRequestDispatcher("/index.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    private SearchResult searchFile(SearchResult lastResult,
                                    SearchRequest request) {
        if (!getReadyForSearch()) {
            return null;
        }
        if (request == null || request.keyword.isEmpty())
            return null;
        if (request.searchType == SearchRequest.REQUEST_SEARCHTYPE_CONTINUE
                && lastResult == null)
            return null;

        // *******************************************************************
        Query query = null;
        Query fQuery;
        try {
            fQuery = fileNameParser.parse(QueryParserBase.escape(request.keyword));
            switch (request.fileType) {
                case SearchRequest.REQUEST_FILETYPE_ALL: {
                    query = fQuery;
                    break;
                }
                case SearchRequest.REQUEST_FILETYPE_FILE: {
                    Query tQuery = isDirParser.parse("false");
                    BooleanQuery bQuery = new BooleanQuery();
                    bQuery.add(fQuery, BooleanClause.Occur.MUST);
                    bQuery.add(tQuery, BooleanClause.Occur.MUST);
                    query = bQuery;
                    break;
                }
                case SearchRequest.REQUEST_FILETYPE_DIR: {
                    Query tQuery = isDirParser.parse("true");
                    BooleanQuery bQuery = new BooleanQuery();
                    bQuery.add(fQuery, BooleanClause.Occur.MUST);
                    bQuery.add(tQuery, BooleanClause.Occur.MUST);
                    query = bQuery;
                    break;
                }
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        if (query == null) {
            return null;
        }

        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter(
                "<span class=\"highlight\">", "</span>");
        Highlighter highlighter = new Highlighter(formatter, new QueryScorer(
                fQuery));
        highlighter.setTextFragmenter(new SimpleFragmenter(60));
        // *******************************************************************
        try {
            SearchResult newResult;
            if (lastResult == null) {
                newResult = new SearchResult(request);
            } else {
                newResult = lastResult;
                newResult.currentRequest = request;
            }

            switch (request.searchType) {
                case SearchRequest.REQUEST_SEARCHTYPE_CONTINUE: {// 继续搜索
                    int targetPage = Math.min(request.jumpToPage,
                            newResult.totalPages);
                    newResult.currentPage = targetPage;
                    // 先计算此次请求需要的记录位置
                    int cHit_first = (targetPage - 1) * HIT_PER_PAGE;
                    // 统计缓存需要的条目数
                    int cache_first;
                    int cache_last;
                    if ((cHit_first % SearchResult.CACHED_RESULT_COUNT) < (SearchResult.CACHED_RESULT_COUNT / 2)) {
                        // 向前缓存
                        cache_first = (cHit_first
                                / SearchResult.CACHED_RESULT_COUNT - 1)
                                * SearchResult.CACHED_RESULT_COUNT;
                    } else {
                        // 向后缓存
                        cache_first = (cHit_first / SearchResult.CACHED_RESULT_COUNT)
                                * SearchResult.CACHED_RESULT_COUNT;
                    }
                    cache_last = cache_first + SearchResult.CACHED_RESULT_COUNT * 2
                            - 1;// 即为需要检索到的条目数
                    // 验证缓存上下限
                    int cache_offset = 0;
                    if (cache_first < 0) {
                        cache_offset = -cache_first;
                        cache_first = 0;
                    }
                    if (cache_last > newResult.totalResults - 1)
                        cache_last = newResult.totalResults - 1;
                    // 验证新的区间是否和老的区间有交叉
                    // if (cache_last < newResult.firstResult_hitnum
                    // || cache_first > newResult.lastResult_hitnum) {
                    // // 没有交集
                    // } else {
                    // // 有交集
                    // // 判断集合是否相等
                    if (cache_first == newResult.firstResult_hitnum
                            && cache_last == newResult.lastResult_hitnum) {
                        // 计算好偏移量(firstHitNum)直接返回
                        newResult.firstHitNum = cHit_first - cache_first
                                + cache_offset;
                        return newResult;
                    } // else {
                    // // 不是完全重合，则可以利用一半缓存
                    // List<ResultDocument> _tmp = newResult.documents_afterward;
                    // newResult.documents_afterward = newResult.documents_forward;
                    // newResult.documents_forward = _tmp;
                    // ScoreDoc _tmp2 = newResult.middleResult;
                    // newResult.middleResult = newResult.lastResult;
                    // newResult.lastResult = _tmp2;
                    // if (cache_first < newResult.firstResult_hitnum) {
                    // // 后半部分重合
                    // newResult.documents_forward.clear();
                    // newResult.middleResult = null;
                    // if(newResult.documents_afterward.size() <
                    // SearchResult.CACHED_RESULT_COUNT){
                    // //没有填满，则必然是总量不足
                    // }
                    // if (newResult.firstResult_hitnum == 0) {// 前面没了
                    //
                    // }
                    // } else {
                    // // 前半部分重合
                    // }
                    // }
                    // }

                    // break;
                }
                case SearchRequest.REQUEST_SEARCHTYPE_NEW: {// 新建搜索
                    // 计算搜索最大需求的页数
                    int targetPage = request.jumpToPage;
                    // 计算要构建目标页面需要的hit的编号范围
                    int cHit_first = (targetPage - 1) * HIT_PER_PAGE;
                    // int cHit_last = cHit_first + HIT_PER_PAGE - 1;
                    // 统计缓存需要的条目数
                    int cache_first;
                    int cache_last;
                    if ((cHit_first % SearchResult.CACHED_RESULT_COUNT) < (SearchResult.CACHED_RESULT_COUNT / 2)) {
                        // 向前缓存
                        cache_first = (cHit_first
                                / SearchResult.CACHED_RESULT_COUNT - 1)
                                * SearchResult.CACHED_RESULT_COUNT;
                    } else {
                        // 向后缓存
                        cache_first = (cHit_first / SearchResult.CACHED_RESULT_COUNT)
                                * SearchResult.CACHED_RESULT_COUNT;
                    }
                    cache_last = cache_first + SearchResult.CACHED_RESULT_COUNT * 2
                            - 1;// 即为需要检索到的条目数

                    TopDocs results = indexSearcher.search(query, cache_last + 1);// 检索
                    // 验证结果数量
                    newResult.totalResults = results.totalHits;
                    newResult.totalPages = (int) Math
                            .ceil((double) newResult.totalResults / HIT_PER_PAGE);
                    if (newResult.totalPages < request.jumpToPage) {// 请求的页面数小于总数
                        targetPage = newResult.totalPages;
                        // 重新计算缓存页数
                        cHit_first = (targetPage - 1) * HIT_PER_PAGE;
                        if ((cHit_first % SearchResult.CACHED_RESULT_COUNT) < (SearchResult.CACHED_RESULT_COUNT / 2)) {
                            // 向前缓存
                            cache_first = (cHit_first
                                    / SearchResult.CACHED_RESULT_COUNT - 1)
                                    * SearchResult.CACHED_RESULT_COUNT;
                        } else {
                            // 向后缓存
                            cache_first = (cHit_first / SearchResult.CACHED_RESULT_COUNT)
                                    * SearchResult.CACHED_RESULT_COUNT;
                        }
                        cache_last = cache_first + SearchResult.CACHED_RESULT_COUNT
                                * 2 - 1;
                    }
                    // 验证缓存上下限
                    int cache_offset = 0;
                    if (cache_first < 0) {
                        cache_offset = -cache_first;
                        cache_first = 0;
                    }
                    if (cache_last > newResult.totalResults - 1)
                        cache_last = newResult.totalResults - 1;

                    // 清空可能存在的结果
                    newResult.documents_forward.clear();
                    newResult.documents_afterward.clear();
                    // 缓存记录
                    for (int i = cache_first; i <= cache_last; i++) {
                        ResultDocument rd = hit2Result(results.scoreDocs[i],
                                highlighter);
                        if (rd == null) {
                            return null;
                        }
                        int _pos = i - cache_first + cache_offset;
                        if (_pos >= SearchResult.CACHED_RESULT_COUNT) {
                            newResult.documents_afterward.add(rd);
                        } else {
                            newResult.documents_forward.add(rd);
                        }
                    }

                    newResult.offset_hitnum = cache_offset;
                    newResult.firstResult_hitnum = cache_first;
                    newResult.lastResult_hitnum = cache_last;
                    if (results.scoreDocs.length != 0)
                        newResult.lastResult = results.scoreDocs[results.scoreDocs.length - 1];
                    if (results.scoreDocs.length > cache_first - cache_offset
                            + SearchResult.CACHED_RESULT_COUNT) {
                        newResult.middleResult = results.scoreDocs[cache_first
                                - cache_offset + SearchResult.CACHED_RESULT_COUNT];
                    }
                    newResult.firstHitNum = cHit_first - cache_first + cache_offset;
                    newResult.currentPage = targetPage;
                    break;
                }
            }
            return newResult;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public ResultDocument hit2Result(ScoreDoc hit, Highlighter highlighter) {
        try {
            ResultDocument rd = new ResultDocument();
            Document doc = indexSearcher.doc(hit.doc);
            rd.displayName = doc.get(FileIndexer.FIELD_FILENAME);
            // String _fs = doc.get(FileIndexer.FIELD_FILESIZE);
            // if (_fs == null)
            // _fs = "0";
            rd.fileSize = Long.valueOf(doc.get(FileIndexer.FIELD_FILESIZE));
            rd.displayUrl = doc.get(FileIndexer.FIELD_PATH);
            rd.displayUrl = rd.displayUrl.replace('\\', '/');
            rd.url = Util.packUrlString(rd.displayUrl);
            if (!url_prefix.isEmpty()) {
                rd.displayUrl = url_prefix + rd.displayUrl;
                rd.url = url_prefix + rd.url;
            }
            rd.displayFatherUrl = rd.displayUrl.substring(0,
                    rd.displayUrl.lastIndexOf(rd.displayName));
            rd.fatherUrl = Util.packUrlString(rd.displayFatherUrl);

            rd.isDir = Boolean.valueOf(doc.get(FileIndexer.FIELD_ISDIR));

            TokenStream tokenStream = analyzer.tokenStream(
                    FileIndexer.FIELD_FILENAME,
                    new StringReader(rd.displayName));
            rd.highlightString = highlighter.getBestFragment(tokenStream,
                    rd.displayName);
            return rd;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidTokenOffsetsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
