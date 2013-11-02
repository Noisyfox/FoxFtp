<%@ page import="FTPSearcher.*" %>
<%@ page import="java.util.Properties" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="utf-8" %>
<%--
  Created by IntelliJ IDEA.
  User: Noisyfox
  Date: 13-9-30
  Time: 下午6:41
  To change this template use File | Settings | File Templates.
--%>
<%
    request.setCharacterEncoding("utf-8");
%>
<!DOCTYPE html>
<html>

<%
    SearchResult searchResult = (SearchResult) request
            .getAttribute("searchResult");

    //载入之前的内容
    String lInput = request.getParameter("keyword");
    if (lInput != null)
        lInput = Util.packHtmlString(lInput.trim());
    else
        lInput = "";
%>

<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>计算中心 FTP 搜索 powered by FoxFtp</title>
    <link rel="stylesheet" href="css/main.css"/>
    <link rel="stylesheet" href="images/typeicon/typeicon-32.css"/>
    <script type="text/javascript" src="js/jquery-1.9.0.min.js"></script>
    <script type="text/javascript" src="js/main.js"></script>
    <script type="text/javascript" src="images/typeicon/typeicon.js"></script>
</head>
<body <%if (searchResult == null || searchResult.totalResults == 0) {%>
        onLoad="document.forms.inputform.keyword.select();" <%}%>>
<div id="wrapper">
<div id="content">
<div id="global-nav">
    <ul class="MenuNav">
        <li id="ftplink" class="dropdown">
            <a>
                <strong>FTP 相关模块</strong>
                <em class="icon-triangle-down-blue"></em>
            </a>
            <ul class="MenuItem">
                <li><a href="http://my.nuaa.edu.cn/forum-206-1.html" target="_blank">FTP 讨论区</a></li>
                <li><a href="http://my.nuaa.edu.cn/forum-167-1.html" target="_blank">FTP 资源发布区</a></li>
                <li><a href="http://my.nuaa.edu.cn/forum-168-1.html" target="_blank">FTP 资源求助区</a></li>
                <li class="split"><a href="http://page.renren.com/601428879" target="_blank">人人主页</a></li>
            </ul>
        </li>
        <li>|</li>
        <li>
            <a href="./management/" target="_blank">FTP 管理</a>
        </li>
    </ul>
</div>

<div id="searchDiv">
    <p id="lg">
        <% if (searchResult != null) { %>
        <a href="./"><img src="images/ftp.png" alt="到搜索首页" title="到搜索首页"/></a>
        <%} else {%>
        <img src="images/ftp.png" alt=""/>
        <%}%>
    </p>

    <form name="inputform" id="searchForm" action="./SearchFtp" method="get">
        <table>
            <tbody>
            <tr>
                <td class="boxLeftBorder"></td>
                <td class="boxMiddleBorder">
                    <label>
                        <input type="text" class="searchText" name="keyword" placeholder="搜点什么？"
                               value="<%=lInput%>"/>
                    </label>
                </td>
                <td class="boxRightBorder" onclick="document.inputform.submit();"></td>
            </tr>
            </tbody>
        </table>
    </form>
    <% //if (searchResult == null) { %>
    <!--
    <div id="hotword">
        <strong>大家都在搜:</strong>
        <a href="./">云图</a>
        <a href="./">火影忍者</a>
        <a href="./">matlab</a>
        <a href="./">进击的巨人</a>
        <a href="./">小爸爸</a>
    </div>
    -->
    <% //} %>
</div>
<% if (searchResult != null) { %>
<!-- Begin Search Result -->
<div id="searchResultDiv">
    <%
        if (searchResult.totalResults == 0) {
    %>
    <div id="searchResultHeader">
        <p>抱歉，没有找到与<span style="color: #CC0000; "><%=Util.packHtmlString("\"") + lInput
                + Util.packHtmlString("\"")%></span>相关的文件。</p>

    </div>
    <%} else {%>
    <div id="searchResultHeader">
        <p>共找到&nbsp;<span
                style="color: #FF0000; "><%=searchResult.totalResults%></span>&nbsp;条结果，耗时&nbsp;<%=searchResult.totalMillisecond%>
            &nbsp;毫秒</p>

    </div>
    <div id="searchResult">
        <table id="searchResultTable">
            <thead>
            <tr>
                <th colspan="2" class="name">文件</th>
                <th class="size">大小</th>
                <th class="date">文件日期</th>
            </tr>
            </thead>
            <tbody>
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
            <tr>
                <td class="typeiconTableCol"><em class="typeicon ti-folder"></em></td>
                <td>
                    <p><a class="file" target="_blank" href="<%=rd.url%>"><%=rd.displayName%>
                    </a></p>

                    <p><a class="dir" target="_blank" href="<%=rd.fatherUrl%>">位于:&nbsp;<%=rd.displayFatherUrl%>
                    </a></p>
                </td>
                <td class="size"><%=rd.isDir ? "-" : Util.packFileSizeString(rd.fileSize)%>
                </td>
                <td class="date">-</td>
            </tr>
            <%
                }
            %>
            </tbody>
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
        <div id="paging">
            <%if (searchResult.currentPage > 1) { %>
            <a href="<%=Util.generatePageUrl(".", lInput, searchResult, searchResult.currentPage - 1)%>"><span
                    class="pc text">&lt;上一页</span></a>
            <% } %>
            <%if (first != 1) { %>
            <a href="<%=Util.generatePageUrl(".", lInput, searchResult, 1)%>"><span
                    class="pc text">首页</span></a>
            <% } %>
            <%
                for (int i = first; i <= last; i++) {
                    if (i != searchResult.currentPage) {
            %>
            <a href="<%=Util.generatePageUrl(".", lInput, searchResult, i)%>"><span
                    class="pc"><%=i%></span></a>
            <%
            } else {
            %>
            <strong><span class="pc"><%=i%></span></strong>
            <%
                    }
                }
            %>
            <%if (last != searchResult.totalPages) {%>
            <a href="<%=Util.generatePageUrl(".", lInput, searchResult, searchResult.totalPages)%>"><span
                    class="pc text">尾页</span></a>
            <%}%>
            <%if (searchResult.currentPage < searchResult.totalPages) { %>
            <a href="<%=Util.generatePageUrl(".", lInput, searchResult, searchResult.currentPage+1)%>"><span
                    class="pc text">下一页&gt;</span></a>
            <%}%>
        </div>
        <%
            }
        %>
    </div>

    <% } %>
</div>
<!-- End Search Result -->
<%
} else {
    Properties sp = ServiceStatusUtil
            .getServiceStatus();
    String url_prefix = sp.getProperty(ServiceStatusUtil.STATUS_URL_PREFIX,
            "").trim();
%>
<!-- Begin Index Information -->
<div id="indexInfoDiv">
    <div id="browseFiles">
        <a href="<%=url_prefix%>">&lt;只是想逛逛就戳我&gt;</a>
    </div>
    <!--
    <div id="fileChanges">
        <table id="fileChangesTable">
            <tr>
                <td>
                    <table id="newFilesTable">
                        <thead>
                        <tr>
                            <th colspan="2">最近更新</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td class="newFileName"><a href="./">aaa.txt</a></td>
                            <td class="newFileTime">2013-11-02</td>
                        </tr>
                        <tr>
                            <td class="newFileName"><a href="./">aaa.txt</a></td>
                            <td class="newFileTime">2013-11-02</td>
                        </tr>
                        <tr>
                            <td class="newFileName"><a href="./">aaa.txt</a></td>
                            <td class="newFileTime">2013-11-02</td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
        </table>
    </div>
    -->
</div>
<!-- End Index Information -->
<% } %>

<div id="spaceholder"></div>
</div>

<div id="footDiv">
    <div id="footDivCon">
        <p>技术支持：<a href="http://my.nuaa.edu.cn/space-uid-339947.html" target="_blank">Noisyfox</a></p>

        <p>站长信箱：<a href="mailto:jnccftp@nuaa.edu.cn">jnccftp@nuaa.edu.cn</a></p>
    </div>
</div>
</div>
</body>
</html>
