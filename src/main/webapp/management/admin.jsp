<%@ page language="java" contentType="text/html; charset=utf-8"
	import="org.wltea.analyzer.lucene.IKAnalyzer,
org.apache.lucene.analysis.Analyzer,
org.apache.lucene.analysis.TokenStream,
org.apache.lucene.analysis.tokenattributes.CharTermAttribute,
java.io.StringReader,
FTPSearcher.ServiceStatuesUtil,
java.util.Properties
"
	pageEncoding="utf-8"%>
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
<script type="text/javascript">
function requestAdmin(request){
	$.post("AdminSrv", request,function(result){
		window.location.reload();
	});
}

function updateSettings(){
	if (confirm("如果你设置了一个错误的路径，那么服务器可能会无法正常工作！你确定要更新服务器设置吗？")){
		requestAdmin({request:'updsettings',ftpdir:$("#ftpdir").val(),indexdir:$("#indexdir").val()});
	}
}
</script>
</head>
<body>
	<h1>FoxFtp管理</h1>
	<p style="border-bottom: 1px solid #F0F0F0; text-align: left;">
		<a>Ftp搜索</a>
	</p>
	<a>状态：</a><br/>
	<%
	Properties serviceStatues = ServiceStatuesUtil.getServiceStatues(getServletContext());
	if(serviceStatues != null){%>
		<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;已归档文件数：<%=serviceStatues.getProperty(ServiceStatuesUtil.STATUES_FILE_TOTAL, "0")%>（<%=serviceStatues.getProperty(ServiceStatuesUtil.STATUES_FILE_FILE, "0")%>文件，<%=serviceStatues.getProperty(ServiceStatuesUtil.STATUES_FILE_DIR, "0")%>目录）</a><br/>
		<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;上次归档统计时间：<%=serviceStatues.getProperty(ServiceStatuesUtil.STATUES_LAST_DOC_TIME, "NULL")%></a><br/>
		<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Ftp归档目录：<%=serviceStatues.getProperty(ServiceStatuesUtil.STATUES_FTP_PATH, "NULL")%></a><br/>
		<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Ftp索引目录：<%=serviceStatues.getProperty(ServiceStatuesUtil.STATUES_INDEX_PATH, "NULL")%></a><br/>
	<%}else{%>
		<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;读取服务器状态失败！</a><br/>
	<%}%>
	<a>设置：</a><br/>
	<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;设置归档目录：</a><input name="ftpdir" id="ftpdir" value="<%=serviceStatues!=null?serviceStatues.getProperty(ServiceStatuesUtil.STATUES_FTP_PATH, ""):"" %>" /><br/>
	<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;设置索引目录：</a><input name="indexdir" id="indexdir" value="<%=serviceStatues!=null?serviceStatues.getProperty(ServiceStatuesUtil.STATUES_INDEX_PATH, ""):"" %>" /><br/>
	<a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<button onclick="updateSettings()"><strong>保存搜索设置</strong></button>&nbsp;&nbsp;<button><strong>刷新搜索索引</strong></button></a><br/>
	<%
		//String text = "测试一下分词的效果哦~ A test of the English analysis.";
		//Analyzer analyzer = new IKAnalyzer();
		//TokenStream ts = analyzer.tokenStream("", new StringReader(text));  
		//CharTermAttribute termAttribute = ts.addAttribute(CharTermAttribute.class);
        //ts.reset();
        //while(ts.incrementToken()){  
        //    System.out.print("["+termAttribute.toString()+"]");  
        //} 
	%>
</body>
</html>