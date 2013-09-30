$(document).ready(function() {
    // :hover patch for IE
    var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
    var isOpera = userAgent.indexOf("Opera") > -1; //判断是否Opera浏览器
    var isIE = userAgent.indexOf("compatible") > -1 && userAgent.indexOf("MSIE") > -1 && !isOpera; //判断是否IE浏览器
    var enable = false;

    if (isIE) {
        var reIE = new RegExp(/MSIE (\d+\.\d+);/);
        reIE.test(userAgent);
        var IEVersion = parseFloat(RegExp["$1"]);
        
        // IE8 supported hover...
        if (IEVersion < 8.0) {
            enable = true;
        }
    }
    //enable = true;
    
    if (enable) {
        $(".MenuNav > li").hover(function() {
            $(this).find(".MenuItem").css("visibility", "visible");
        }, function() {
            $(this).find(".MenuItem").css("visibility", "hidden");
        });

        $(".MenuItem li").hover(function() {
            $(this).css("background", "#eee");
        }, function() {
            $(this).css("background", "#fff");
        });
        
        $(".boxRightBorder").hover(function() {
            $(this).css("background-position", "-143px 0px");
        }, function() {
            $(this).css("background-position", "-31px 0px");
        });
        
        $(".boxRightBorder").mousedown(function() {
            $(this).css("background-position", "-255px 0px");
        });
        
        $(".boxRightBorder").mouseup(function() {
            $(this).css("background-position", "-143px 0px");
        });
    } else {
        console.log("CSS3 patch bypassed");
    }
	
});
