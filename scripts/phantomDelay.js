var page = require('webpage').create();
var fs = require('fs');
var system = require('system');

var url = system.args[1];
var file = system.args[2];

page.open(url, function(status) {

fs.write("hi.txt", concat(url, "boop", file)); 
if(status === "success") {
	window.setTimeout(function () {
			var text_output = page.evaluate(function () {
				return document.body.innerHTML;
			});
			fs.write(file, text_output);   
			phantom.exit();
        }, 20000);
  }
});