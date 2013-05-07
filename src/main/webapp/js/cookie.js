//获得cookie 的值

function addCookie(objName, objValue, objHours) { // 添加cookie

	var str = objName + "=" + escape(objValue);

	if (objHours > 0) { // 为时不设定过期时间，浏览器关闭时cookie自动消失

		var date = new Date();

		var ms = objHours * 3600 * 1000;

		date.setTime(date.getTime() + ms);

		str += "; expires=" + date.toGMTString();

	}

	document.cookie = str;

}

function getCookie(name)// 取cookies函数

{

	var arr = document.cookie
			.match(new RegExp("(^| )" + name + "=([^;]*)(;|$)"));

	if (arr != null)
		return unescape(arr[2]);
	return null;

}

function delCookie(name)// 删除cookie

{

	var exp = new Date();

	exp.setTime(exp.getTime() - 1);

	var cval = getCookie(name);

	if (cval != null)
		document.cookie = name + "=" + cval + ";expires=" + exp.toGMTString();

}
