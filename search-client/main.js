// Dependencies
const express = require('express'),
      bodyParser = require('body-parser'),
      request = require('request'),
      fs = require('fs');

// Express setup
var app = express();
app.use('/static', express.static('public'));
app.use(bodyParser.json());
app.set('view engine', 'jade');
app.set('views', './views')

// Routes
app.get('/', (req, res) => res.render('index'));

app.post('/search', (req, res) => {
  request(`${process.env.FULL_TEXT_SERVICE_URI}/search`, {
    method: 'POST',
    json: req.body,
  }).pipe(res);
});

app.post('/terms', (req, res) => {
  request(`${process.env.FULL_TEXT_SERVICE_URI}/terms`, {
    method: 'POST',
    json: req.body,
  }).pipe(res);
});

app.get('/download/:path', (req, res) =>{
  var path = req.params.path;
  res.download(path, (err) => {
    if(err) {
      res.writeHead(404);
      res.end();
    }
  });
});

// Special exception handler
process.on('uncaughtException', error => console.error('ERROR - Exception ', error));

// Graceful shutdown
function signalHandler(signal) {
    process.kill(process.pid, signal);
};
process.once('SIGUSR2', () => signalHandler('SIGUSR2')); // Nodemon
process.once('SIGINT', () => signalHandler('SIGINT')); // PM2

// Run
app.listen(process.env.PORT || 3030, () => console.log("RUNNING - Application running on port 3030"));