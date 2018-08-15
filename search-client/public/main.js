require.config({
  baseUrl: 'static/',
  waitSeconds: 100,
  shim: {
      'bootstrap': {'deps': ['jquery']},
      'underscore': { exports: '_'},
      'backbone': {'deps': ['underscore'], exports: 'Backbone'}
  },
  paths: {
      'jquery': 'lib/jquery/jquery-min',
      'text': 'lib/requirejs/requirejs-text',
      'bootstrap': 'lib/bootstrap/js/bootstrap.bundle.min',
      'wordcloud': 'lib/wordcloud/wordcloud-min',
      'underscore': 'lib/underscore/underscore-min',
      'backbone': 'lib/backbone/backbone-min',
  }
});

require(['app/view/search'],
    function (SearchView) {
         new SearchView();
    }
);