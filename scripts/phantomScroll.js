var page = require('webpage').create();
var fs = require('fs');
var system = require('system');

var url = system.args[1];
var file = system.args[2];
page.open(url, function() {
    var count = 0;
    var text = 'hi';
    window.setInterval(function() {
	if(count < 5) {
	    text = "internal";
	    count = count + 1;
            text_output = page.evaluate(function() {
                window.document.body.scrollTop = document.body.scrollHeight;
            });
      	} else { 
	    fs.write(file, text_output);
            phantom.exit();
        }
    }, 500);
});