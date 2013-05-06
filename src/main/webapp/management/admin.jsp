<%@ page language="java" contentType="text/html; charset=utf-8"
	import="org.wltea.analyzer.lucene.IKAnalyzer,
org.apache.lucene.analysis.Analyzer,
org.apache.lucene.analysis.TokenStream,
org.apache.lucene.analysis.tokenattributes.CharTermAttribute,
java.io.StringReader"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
	request.setCharacterEncoding("utf-8");
%>
<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>FoxFtp管理</title>
</head>
<body>
	<%
		String text = "测试一下分词的效果哦~ A test of the English analyze.";
		Analyzer analyzer = new IKAnalyzer();
		TokenStream ts = analyzer.tokenStream("", new StringReader(text));  
		CharTermAttribute termAttribute = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while(ts.incrementToken()){  
            System.out.print("["+termAttribute.toString()+"]");  
        } 
	%>
</body>
</html>