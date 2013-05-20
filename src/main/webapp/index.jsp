<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8"
         import="FTPSearcher.SearchResult,
                 FTPSearcher.Util,
                 FTPSearcher.SearchFtp,
                 FTPSearcher.ResultDocument,
                 FTPSearcher.SearchRequest" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
    request.setCharacterEncoding("utf-8");
%>
<html>

<%
    SearchResult searchResult = (SearchResult) request
            .getAttribute("searchResult");
%>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>计算中心FTP搜索 powered by FoxFtp</title>
    <link rel="icon" href="images/ftp_icon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="images/ftp_icon.ico" type="image/x-icon"/>
    <link rel="stylesheet" href="css/menu.css"/>
    <link rel="stylesheet" href="css/main.css"/>

    <script type="text/javascript" src="js/jquery-1.6.3.min.js"></script>
    <script type="text/javascript" src="js/search.js"></script>
    <script type="text/javascript" src="js/menu.js"></script>

    <%if (searchResult != null) { %>
    <link rel="stylesheet" href="css/searchresult.css"/>
    <script type="text/javascript" src="js/result.js"></script>
    <%} %>
</head>

<body <%if (searchResult == null || searchResult.totalResults == 0) {%>
        onLoad="document.forms.inputform.keyword.focus()" <%}%>>
<p style="border-bottom: 1px solid #F0F0F0; text-align: right;">
    <a class=downlist id=ftp_more onMouseOver="MM_safe_mousein(1)"
       onmouseout="MM_safe_mouseout(this,event,1)">FTP相关板块 </a>|<a
        class=head href="./management/" target="_blank">FTP管理</a>
</p>

<div id=s_related_menu class=s_menu
     style="visibility: hidden; right: 59px; + *right: 57px; _right: 56px;"
     onMouseOver="MM_safe_mousein(1)"
     onmouseout="MM_safe_mouseout(this,event,1)">

    <a href="http://my.nuaa.edu.cn/forum-206-1.html" target="_blank">FTP讨论区</a> <a
        href="http://my.nuaa.edu.cn/forum-167-1.html" target="_blank">FTP资源发布区</a> <a
        href="http://my.nuaa.edu.cn/forum-168-1.html" target="_blank">FTP资源求助区</a> <a
        class=sep style="overflow: hidden"
        href="http://page.renren.com/601428879" target="_blank">人人主页</a>

</div>
<%
    if (searchResult == null) {
        for (int i = 0; i < 7; i++) {
%>
<br/>
<%
        }
    }
%>
<div align="center">
    <%
        if (searchResult == null) {
    %>
    <img src="./images/ftp.jpg" alt=""/>
    <%} else { %>
    <a href="/"><img src="./images/ftp.jpg" alt="到搜索首页" title="到搜索首页"/></a>
    <%} %>
</div>
<%
    //载入之前的内容
    String lInput = request.getParameter("keyword");
    if (lInput != null)
        lInput = Util.packHtmlString(lInput.trim());
    else
        lInput = "";
%>
<div align="center">
    <form name="inputform" id="form1" action="./SearchFtp" method="get">
        <table cellpadding="0" cellspacing="0">
            <tbody>
            <tr>
                <td class="td_head"></td>
                <td class="td_text"><input class="inputtext" name="keyword"
                                           value="<%=lInput%>"/></td>
                <td class="td_search">
                    <button class="td_submit" type="submit">
                        <strong>搜索</strong>
                    </button>
                </td>
            </tr>
            </tbody>
        </table>
    </form>
</div>
<%
    if (searchResult != null) {
%>
<div class="resheader"></div>
<div class="neck">
    <div id="resultcounts">
        <%
            if (searchResult.totalResults == 0) {
        %>
        <a>抱歉，没有找到与<span style="color: #CC0000; "><%=Util.packHtmlString("\"") + lInput
                + Util.packHtmlString("\"")%></span>相关的文件。
        </a>
        <%
        } else {
        %>
        <a>共找到&nbsp;<span
                style="color: #FF0000; "><%=searchResult.totalResults%></span>&nbsp;条结果，耗时&nbsp;<%=searchResult.totalMillisecond%>
            &nbsp;毫秒
        </a>
        <%
            }
        %>
    </div>
</div>
<%
    if (searchResult.totalResults != 0) {
%>
<div>
    <table class="content" id="result" cellpadding="0" cellspacing="0">
        <tr>
            <th class="resultsHeader">文件名</th>
            <th class="resultsHeader">大小</th>
        </tr>
        <%
            for (int i = 0; i < SearchFtp.HIT_PER_PAGE; i++) {
                ResultDocument rd;
                if (i + searchResult.firstHitNum < SearchResult.CACHED_RESULT_COUNT) {
                    if (i + searchResult.firstHitNum > searchResult.documents_forward
                            .size() - 1)
                        break;
                    rd = searchResult.documents_forward.get(i
                            + searchResult.firstHitNum);
                } else if (i + searchResult.firstHitNum < SearchResult.CACHED_RESULT_COUNT * 2) {
                    if (i + searchResult.firstHitNum
                            - SearchResult.CACHED_RESULT_COUNT > searchResult.documents_afterward
                            .size() - 1)
                        break;
                    rd = searchResult.documents_afterward.get(i
                            + searchResult.firstHitNum
                            - SearchResult.CACHED_RESULT_COUNT);
                } else
                    break;
        %>
        <tbody>
        <tr class="resultsRow">
            <td class="td" align="left">
                <a href="<%=rd.url%>" target="_blank">
                    <img src="<%=rd.isDir?"./images/diritem.gif":"./images/file.gif"%>" alt=""/>
                    <%=rd.displayName%>
                </a><br/>
                <a class="dir" href="<%=rd.fatherUrl%>" target="_blank">
                    <%="位于:" + rd.displayFatherUrl%>
                </a>
            </td>
            <%if (rd.isDir) { %>
            <td align="center">
                --
            </td>
            <%} else { %>
            <td align="right">
                <%=Util.packFileSizeString(rd.fileSize)%>
            </td>
            <%} %>
        </tr>
        </tbody>
        <%
            }
        %>
    </table>
    <%
        if (searchResult.totalPages > 1) {
            int first, last;
            if (searchResult.totalPages <= 10) {
                first = 1;
                last = searchResult.totalPages;
            } else {
                if (searchResult.currentPage <= 4) {
                    first = 1;
                    last = 10;
                } else if (searchResult.totalPages - searchResult.currentPage <= 5) {
                    first = searchResult.totalPages - 9;
                    last = searchResult.totalPages;
                } else {
                    first = searchResult.currentPage - 4;
                    last = first + 9;
                }
            }
    %>
    <div class="paging">
        <%if (searchResult.currentPage > 1) { %>
        <a href="<%="SearchFtp?page="+(searchResult.currentPage-1)+"&keyword="+lInput+"&fileType="+SearchRequest.getFileTypeString(searchResult.currentRequest.fileType)%>">
            &lt;上一页</a>
        <%} %>
        <%if (first != 1) {%>
        <a href="<%="SearchFtp?page=1&keyword="+lInput+"&fileType="+SearchRequest.getFileTypeString(searchResult.currentRequest.fileType)%>">首页</a>
        <%} %>
        <%
            for (int i = first; i <= last; i++) {
                if (i == searchResult.currentPage) {
        %>&nbsp;<span style="font-weight: bold;"><%=i%>
    </span>&nbsp;<%
    } else {
    %><a href="<%="SearchFtp?page="+i+"&keyword="+lInput+"&fileType="+SearchRequest.getFileTypeString(searchResult.currentRequest.fileType)%>"
         class="ah"><%=i%>
    </a><%
            }
        }
    %>
        <%if (last != searchResult.totalPages) {%>
        <a href="<%="SearchFtp?page="+searchResult.totalPages+"&keyword="+lInput+"&fileType="+SearchRequest.getFileTypeString(searchResult.currentRequest.fileType)%>">尾页</a>
        <%} %>
        <%if (searchResult.currentPage < searchResult.totalPages) { %>
        <a href="<%="SearchFtp?page="+(searchResult.currentPage+1)+"&keyword="+lInput+"&fileType="+SearchRequest.getFileTypeString(searchResult.currentRequest.fileType)%>">下一页&gt;</a>
        <%} %>
    </div>
    <%
        }
    %>


</div>
<%
    }
%>
<%
} else {
    for (int i = 0; i < 10; i++) {
%>
<br/>
<%
        }
    }
%>

<div class="footer_r">
    <br/> <br/><br/>
    <hr/>
    <div class="footer">
        <p>
            技术支持：<a
                href="http://my.nuaa.edu.cn/space-uid-171410.html" target="_blank">忆一段往事</a>&nbsp;&amp;&nbsp;<a
                href="http://my.nuaa.edu.cn/space-uid-339947.html" target="_blank">Noisyfox</a>
        </p>

        <p>
            站长邮箱：<a href="mailto://jnccftp@nuaa.edu.cn">jnccftp@nuaa.edu.cn</a>
        </p>
        <br/> <br/> <br/> <br/>
    </div>
</div>

</body>

</html>