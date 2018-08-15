// Dependencies
try {
	var express = require('express'), 			//http://expressjs.com/api.html 
		bodyParser = require('body-parser');	// https://github.com/expressjs/body-parser	
		request = require('request'), 			// https://github.com/request/request
		fs = require('fs')
} catch(exception) {
 	console.log ("ERROR - Couldn't load some dependancies, run 'npm update' inside directory", exception);
  	process.exit(1);
  	throw new Error("Exiting");
}

// Application bootstrap
var app = express();

// View setup
app.set('view engine', 'jade');
app.set('views', './views')

// Middleware setup
app.use('/static', express.static('public'));
app.use(bodyParser.json()); 

// Routing setup
app.get('/', function(req, res) {	
	res.render('index');
});

app.post('/search', function(req, res) {
	request('http://localhost:9090/search', {
		method: 'POST',
		json: req.body,
	}).pipe(res);	
});

app.post('/terms', function(req, res) {
	request('http://localhost:9090/terms', {
		method: 'POST',
		json: req.body,
	}).pipe(res);	
});

app.get('/download/:path', function(req, res){
	var path = req.params.path;
    
	res.download(path, function(err){
		if(err) {
			res.writeHead(404);	   
			res.end();
		}	
	});
});

// Special exception handler (NodeJS)
process.on('uncaughtException', function(error) {
	console.error('ERROR - Exception ', error);
});

// Run
app.listen(3030, function(){
	console.log("RUNNING - Application running on port 3030");
});