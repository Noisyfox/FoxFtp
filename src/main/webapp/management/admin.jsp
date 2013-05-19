<%@ page language="java" contentType="text/html; charset=utf-8"
         import="FTPSearcher.ServiceStatuesUtil,
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
    <script type="text/javascript" src="../js/jquery-1.6.3.min.js"></script>
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
            str = getCookie("requireStatus");
            if (str != null) {
                var obj = JSON.parse(str);
                showTopMessage(true, obj.success, obj.message);
            }
            delCookie("requireStatus");
        });

        function requestAdmin(request, callback) {
            $.post("AdminSrv", request, callback);
        }

        function updateSettings() {
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
    Properties serviceStatues = ServiceStatuesUtil
            .getServiceStatues(getServletContext());
    if (serviceStatues != null) {
%>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;已归档文件数：<%=serviceStatues.getProperty(
        ServiceStatuesUtil.STATUES_FILE_TOTAL, "0")%>（<%=serviceStatues.getProperty(
        ServiceStatuesUtil.STATUES_FILE_FILE, "0")%>文件，<%=serviceStatues.getProperty(
        ServiceStatuesUtil.STATUES_FILE_DIR, "0")%>目录）
</a>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;上次归档统计时间：<%=serviceStatues.getProperty(
        ServiceStatuesUtil.STATUES_LAST_DOC_TIME, "NULL")%>
</a>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Ftp&nbsp;归档目录：<%=serviceStatues.getProperty(
        ServiceStatuesUtil.STATUES_FTP_PATH, "NULL")%>
</a>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Ftp&nbsp;索引目录：<%=serviceStatues.getProperty(
        ServiceStatuesUtil.STATUES_INDEX_PATH, "NULL")%>
</a>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Ftp&nbsp;URL前缀：&nbsp;<%=serviceStatues.getProperty(
        ServiceStatuesUtil.STATUES_URL_PREFIX, "NULL")%>
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
<input name="ftpdir" id="ftpdir"
       value="<%=serviceStatues != null ? serviceStatues.getProperty(
					ServiceStatuesUtil.STATUES_FTP_PATH, "") : ""%>"/>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;设置索引目录：</a>
<input name="indexdir" id="indexdir"
       value="<%=serviceStatues != null ? serviceStatues.getProperty(
					ServiceStatuesUtil.STATUES_INDEX_PATH, "") : ""%>"/>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;设置URL前缀：&nbsp;</a>
<input name="urlprefix" id="urlprefix"
       value="<%=serviceStatues != null ? serviceStatues.getProperty(
					ServiceStatuesUtil.STATUES_URL_PREFIX, "") : ""%>"/>
<br/>
<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <button onclick="updateSettings()">
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