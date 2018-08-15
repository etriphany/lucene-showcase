'use strict';

define([
  'bootstrap',
  'underscore',
  'backbone',
  'app/model/search-request',
  'app/model/search-response',
  'app/view/result',
  'text!app/template/query-syntax.html',
  'text!app/template/search-form.html'
], function (bootstrap, _, Backbone, SearchRequest, SearchRespone, ResultView, syntaxTemplate, formTemplate) {

  var singleton;

  // TODO: Create a model for it
  function getLanguages() {
    return [
        { iso:"de", name: "German" },
        { iso:"en", name: "English" },
        { iso:"es", name: "Spanish" },
        { iso:"fr", name: "French" },
        { iso:"it", name: "Italian" },
        { iso:"ja", name: "Japonese" },
        { iso:"pt", name: "Portuguese" }
    ]
  }

  var SearchView = Backbone.View.extend({
    el: '#query',

    events: {
      'change #queryText': 'setQuery',
      'change #queryLang': 'setLanguage',
      'click #queryAction': 'search'
    },

    // Initialize view
    initialize: function() {
      this.model = new SearchRequest();
      this.installSearch();
      this.installHelp();
    },

    // Bind help logic
    installHelp: function() {
      var template = _.template(syntaxTemplate);

      this.$('#queryHelp').popover({
        trigger: 'hover',
        container: 'body',
        title: 'Sintax',
        content: template({}),
        html: true,
        placement: 'bottom'
      });
    },

    // Bind search logic
    installSearch: function() {
      var template = _.template(formTemplate);
      $('#query').html(template({languages: getLanguages()}));

      _.bindAll(this, 'setQuery', 'setLanguage', 'search'); // 'this' binder
      this.$queryText = this.$('#queryText');
      this.$queryLang = this.$('#queryLang');
      this.$messages = $('#messages');
      this.model.set('query', this.$queryText.val());
      this.model.set('languages', [this.$queryLang.val()]);
      this.model.set('detectLanguage', !this.$queryLang);
    },

    // Update search text inputs
    setQuery: function() {
      this.model.set('query', this.$queryText.val());
    },

    // Update search language inputs
    setLanguage: function() {
      if(!this.$queryLang.val()) {
        this.model.set('detectLanguage', true);
        this.model.set('languages', []);
      } else {
        this.model.set('detectLanguage', false);
        this.model.set('languages', [this.$queryLang.val()]);
      }
    },

    // Search
    search: function(event, more) {
      if(!singleton) {
        singleton = new ResultView({model: new SearchRespone()});
      } else if(!more) {
        this.model.set('deep', null);
        singleton.reset();
      }

      if(this.model.isValid()) {
        singleton.model.fetch({
          type: 'post',
          contentType: 'application/json; charset=utf-8',
          dataType: 'json',
          data: JSON.stringify(this.model.toJSON()),
        });
      } else {
        this.$messages.find('p').html(this.model.validationError);
        this.$messages.modal('show');
      }
    }
  });

  // Export
  return SearchView
});