<%@ page language="java" contentType="text/html; charset=utf-8"
         import="FTPSearcher.ServiceStatusUtil,
                 java.util.Properties"
         pageEncoding="utf-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
    //session.setAttribute("user_admin", "true");
    request.setCharacterEncoding("utf-8");
%>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>FoxFtp管理</title>
    <script type="text/javascript" src="../js/jquery-1.9.0.min.js"></script>
    <script type="text/javascript" src="../js/cookie.js"></script>
    <script type="text/javascript">
        function showTopMessage(show, success, messages) {
            var messageLable = document.getElementById('messageLabel');
            var message = document.getElementById('message');
            if (show) {
                messageLable.setAttribute("style", "");
                if (success) {
                    messageLable.setAttribute("bgcolor", "#EEEEEE");
                } else {
                    messageLable.setAttribute("bgcolor", "#FF4848");
                }
                message.innerHTML = messages;
            } else {
                messageLable.setAttribute("style", "dispaly:none;");
            }
        }

        $(document).ready(function () {
            var str = getCookie("requireStatus");
            if (str != null) {
                var obj = JSON.parse(str);
                showTopMessage(true, obj.success, obj.message);
            }
            delCookie("requireStatus");
        });

        function requestAdmin(request, callback) {
            $.post("AdminSrv", request, callback);
        }

        function updateServerSettings() {
            if (confirm("如果你设置了一个错误的路径，那么服务器可能会无法正常工作！你确定要更新服务器设置吗？")) {
                requestAdmin({
                    request: 'updsettings',
                    ftpdir: $("#ftpdir").val(),
                    indexdir: $("#indexdir").val(),
                    urlprefix: $("#urlprefix").val()
                }, function (result) {
                    addCookie("requireStatus", result, -1);
                    window.location.reload();
                });
            }
        }

        function reindex() {
            if (confirm("在更新索引的时候将会使搜索服务暂时中断，建议选择一个用户较少的时间段进行本操作。你确定要重新建立搜索索引吗？")) {
                showTopMessage(true, true, "正在重建索引...");
                requestAdmin({
                    request: 'reindex'
                }, function (result) {
                    addCookie("requireStatus", result, -1);
                    window.location.reload();
                });
            }
        }
    </script>
</head>
<body topmargin="0">
<table style="display: none;" id="messageLabel" width="100%"
       bgcolor="#EEEEEE">
    <tr>
        <td valign="top"><a id="message">文字</a></td>
    </tr>
</table>
<h1>FoxFtp管理</h1>

<p style="border-bottom: 1px solid #F0F0F0; text-align: left;">
    <a>Ftp搜索</a>
</p>
<a>状态：</a>
<br/>
<%
    Properties serviceStatus = ServiceStatusUtil
            .getServiceStatus();
        if (serviceStatus != null) {
%>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;已归档文件数：<%=serviceStatus.getProperty(
        ServiceStatusUtil.STATUS_FILE_TOTAL, "0")%>（<%=serviceStatus.getProperty(
        ServiceStatusUtil.STATUS_FILE_FILE, "0")%>文件，<%=serviceStatus.getProperty(
        ServiceStatusUtil.STATUS_FILE_DIR, "0")%>目录）
</a>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;上次归档统计时间：<%=serviceStatus.getProperty(
        ServiceStatusUtil.STATUS_LAST_DOC_TIME, "NULL")%>
</a>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Ftp&nbsp;归档目录：<%=serviceStatus.getProperty(
        ServiceStatusUtil.STATUS_FTP_PATH, "NULL")%>
</a>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Ftp&nbsp;索引目录：<%=serviceStatus.getProperty(
        ServiceStatusUtil.STATUS_INDEX_PATH, "NULL")%>
</a>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Ftp&nbsp;URL前缀：&nbsp;<%=serviceStatus.getProperty(
        ServiceStatusUtil.STATUS_URL_PREFIX, "NULL")%>
</a>
<br/>
<%
} else {
%>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;读取服务器状态失败！</a>
<br/>
<%
    }
%>
<a>设置：</a>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;设置归档目录：</a>
<label for="ftpdir"></label><input name="ftpdir" id="ftpdir"
       value="<%=serviceStatus != null ? serviceStatus.getProperty(
					ServiceStatusUtil.STATUS_FTP_PATH, "") : ""%>"/>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;设置索引目录：</a>
<label for="indexdir"></label><input name="indexdir" id="indexdir"
       value="<%=serviceStatus != null ? serviceStatus.getProperty(
					ServiceStatusUtil.STATUS_INDEX_PATH, "") : ""%>"/>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;设置URL前缀：&nbsp;</a>
<label for="urlprefix"></label><input name="urlprefix" id="urlprefix"
       value="<%=serviceStatus != null ? serviceStatus.getProperty(
					ServiceStatusUtil.STATUS_URL_PREFIX, "") : ""%>"/>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <button onclick="updateServerSettings()">
        <strong>保存搜索设置</strong>
    </button>
    &nbsp;&nbsp;
    <button onclick="reindex()">
        <strong>刷新搜索索引</strong>
    </button>
</a>
<br/>
</body>
</html>