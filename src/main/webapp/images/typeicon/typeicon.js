typeicon_map = {
	"mov": "ti-mp4",
	"avi": "ti-mp4",
	"mkv": "ti-mp4",
	"rm": "ti-mp4",
	"rmvb": "ti-mp4",
	"flv": "ti-mp4",
	"f4v": "ti-mp4",
	"mpg": "ti-mp4",
	"mp4": "ti-mp4",
	"zip": "ti-rar",
	"rar": "ti-rar",
	"m4a": "ti-mp3",
	"flac": "ti-mp3",
	"wav": "ti-mp3",
	"ape": "ti-mp3",
	"mp3": "ti-mp3",
	"file": "ti-file",
	"pptx": "ti-ppt",
	"ppt": "ti-ppt",
	"txt": "ti-txt",
	"7z": "ti-7z",
	"bmp": "ti-img",
	"jpg": "ti-img",
	"jpeg": "ti-img",
	"gif": "ti-img",
	"png": "ti-img",
	"img": "ti-img",
	"chm": "ti-chm",
	"ttf": "ti-ttf",
	"htm": "ti-html",
	"html": "ti-html",
	"folder": "ti-folder",
	"reg": "ti-reg",
	"psd": "ti-psd",
	"js": "ti-js",
	"sys": "ti-sys",
	"c": "ti-c",
	"exe": "ti-exe",
	"lib": "ti-lib",
	"h": "ti-h",
	"iso": "ti-iso",
	"docx": "ti-doc",
	"doc": "ti-doc",
	"cpp": "ti-cpp",
	"pdf": "ti-pdf",
	"xlsx": "ti-xls",
	"xls": "ti-xls",
	"torrent": "ti-torrent",
	"inf": "ti-ini",
	"ini": "ti-ini",
	"":""
};

$(document).ready(function () {
	$(".typeiconTableCol").each(function () {
		var node = $(this).next().find("a");
		var a = node.attr("href");
		var ext = "file";
		if (!a) return;
		
		if (a.substr(a.length - 1, 1) === "/") {
			ext = "folder";
		} else {
			if (a.lastIndexOf(".") >= 0) {
				ext = a.slice(a.lastIndexOf(".") + 1, a.length).toLowerCase();
				if (!typeicon_map[ext]) ext = "file";
			}
		}
		$(this).find(".typeicon").removeClass("ti-folder").addClass(typeicon_map[ext]);
	});
})
