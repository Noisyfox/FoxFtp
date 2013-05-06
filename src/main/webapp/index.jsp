<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
	request.setCharacterEncoding("utf-8");
%>
<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>计算中心FTP搜索 powered by FoxFtp</title>
<link rel="icon" href="images/ftp_icon.ico" type="image/x-icon" />
<link rel="shortcut icon" href="images/ftp_icon.ico" type="image/x-icon" />
<link rel="stylesheet" href="css/menu.css" />

<script type="text/javascript" src="js/jquery-1.6.3.min.js"></script>
<script type="text/javascript" src="js/search.js"></script>
<script type="text/javascript" src="js/menu.js"></script>

<style type="text/css">
body,div,p,ul,li,form,h1,h2 {
	margin: 0;
	padding: 0;
}

.td_head {
	background: url("./images/search_1.jpg");
	width: 11px;
	height: 35px;
}

.td_text {
	background: url("./images/search_2.jpg");
	width: 438px;
	height: 35px;
}

.inputtext {
	width: 435px;
	border: 1px;
	border-bottom-style: none;
	border-top-style: none;
	border-left-style: none;
	border-right-style: none;
}

.td_search {
	background: url("./images/search_3_1.jpg");
	width: 107px;
	height: 35px;
}

.td_submit {
	width: 107px;
	height: 35px;
	opacity: 0;
	filter: alpha(opacity =     0);
	cursor: pointer;
}

.downlist {
	cursor: pointer;
	line-height: 20px;
	font-size: 12px;
	font-family: arial, \5b8b\4f53, sans-serif;
	color: #00C;
	font-weight: bold;
	padding-right: 11px;
	background: url(./images/spis_9762e054.png) no-repeat right -221px
}

.head {
	cursor: pointer;
	line-height: 20px;
	font-size: 12px;
	font-family: arial, \5b8b\4f53, sans-serif;
	color: #00C;
	padding-right: 11px;
}

.footer {
	font-size: 13px;
	text-align: center;
	position: absolute;
	left: 50%;
	width: 760px;
	margin-left: -380px;
}

.footer a {
	color: #666666;
	text-decoration: none;
}

.footer a:link {
	color: #666666;
}

.footer a:hover {
	text-decoration: underline;
}
</style>
</head>

<body onLoad="document.forms.inputform.textbox.focus()">
	<p id=u style="border-bottom: 1px solid #F0F0F0; text-align: right;">
		<a class=downlist id=ftp_more onMouseOver="MM_safe_mousein(1)"
			onmouseout="MM_safe_mouseout(this,event,1)">FTP相关板块 </a>|<a
			class=head id=ftp_more href="./management/">FTP管理</a>
	<div id=s_related_menu class=s_menu
		style="visibility: hidden; right: 59px; + *right: 57px; _right: 56px;"
		onMouseOver="MM_safe_mousein(1)"
		onmouseout="MM_safe_mouseout(this,event,1)">

		<a href="http://my.nuaa.edu.cn/forum-206-1.html">FTP讨论区</a> <a
			href="http://my.nuaa.edu.cn/forum-167-1.html">FTP资源发布区</a> <a
			href="http://my.nuaa.edu.cn/forum-168-1.html">FTP资源求助区</a> <a
			class=sep style="overflow: hidden"
			href="http://page.renren.com/601428879">人人主页</a>

	</div>

	<div align="center">
		<img src="./images/ftp.jpg">
	</div>
	<%
		//载入之前的内容
		String lInput = request.getParameter("textbox");
		if(lInput != null)lInput = lInput.trim();
		else lInput = "";
	%>
	<div align="center">
		<form name="inputform" id="form1" action="./SearchFtp" method="post">
			<table cellpadding="0" cellspacing="0">
				<tbody>
					<tr>
						<td class="td_head"></td>
						<td class="td_text"><input class="inputtext" name="textbox"
							value="<%=lInput%>" /></td>
						<td class="td_search"><button class="td_submit" type="submit">
								<strong>搜索</strong>
							</button></td>
					</tr>
				</tbody>
			</table>



		</form>
	</div>
	<div>
		<%
			String results = (String) request.getAttribute("results");
			if (results != null) {
		%>
		<a><%=results%></a>
		<%
			}
		%>
	</div>

	<br />
	<br />
	<br />
	<hr>

	<div class="footer">
		<p class="copy">
			Copyright&nbsp;@&nbsp;2013&nbsp;<a href="http://jncc.nuaa.edu.cn">计算中心</a>
			&nbsp;&nbsp;&nbsp;by&nbsp;<a
				href="http://my.nuaa.edu.cn/space-uid-171410.html">忆一段往事</a>&nbsp;&amp;&nbsp;<a
				href="http://my.nuaa.edu.cn/space-uid-339947.html">Noisyfox</a>
		</p>
		<p class="author">站长邮箱&nbsp;:&nbsp;<a href="mailto://jnccftp@nuaa.edu.cn">jnccftp@nuaa.edu.cn</a></p>
	</div>

</body>

</html>