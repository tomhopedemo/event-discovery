var page = require('webpage').create();
var fs = require('fs');
var system = require('system');

var url = system.args[1];
var file = system.args[2];

page.open(url, function(status) {
console.log("Status: " + status);
if(status === "success") {

	window.setTimeout(function () {
		await page.waitForSelector("#book_tickets_button", {visible: true});
		const [response] = await Promise.all([
  			page.waitForNavigation(),
  			page.click("#book_tickets_button"),
		]);
        }, 5000);

	window.setTimeout(function () {
			var text_output = page.evaluate(function () {
				return document.body.innerHTML;
			});
			fs.write(file, text_output);   
			phantom.exit();
        }, 20000);

  }
});