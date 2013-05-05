// JavaScript Document
$(document).ready(function() {
	$(".td_search").hover(function() {
		$(".td_search").css("background", "url(images/search_3_2.jpg)");
	}, function() {
		$(".td_search").css("background", "url(images/search_3_1.jpg)");
	});
	$(".td_search").mousedown(function() {
		$(".td_search").css("background", "url(images/search_3_3.jpg)");
	});
	$(".td_search").mouseup(function() {
		$(".td_search").css("background", "url(images/search_3_2.jpg)");
	});
});