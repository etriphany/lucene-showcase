'use strict';

define([
  'bootstrap',
  'underscore',
  'backbone',
  'app/model/terms-request',
  'app/model/terms-response',
  'app/view/terms',
  'text!app/template/search-result.html'
], function (bootstrap, _, Backbone, TermsRequest, TermsResponse, TermsView, resultTemplate) {

  var ResultView = Backbone.View.extend({
    el: '#result',

    events: {
      'click #moreResults': 'moreResults',
      'click .terms-finder': 'showTerms'
    },

    template: _.template(resultTemplate, {variable: 'data'}),

    // Initialize view
    initialize: function() {
      _.bindAll(this, 'render'); // 'this' binder
      this.model.on('change', this.render);
    },

    // Render view
    render: function() {
      this.$el.find('#entries').append(this.template(this.model.toJSON()));

      // No more results
      if(!this.model.get('deep')) {
        this.$el.find('#moreResults').addClass('invisible');
      } else {
        this.$el.find('#moreResults').removeClass('invisible');
      }

      // Show results if hidden
      if(this.$el.hasClass('invisible')) {
        this.$el.removeClass('invisible');
      }

        return this;
    },

    // Reset view
    reset: function() {
      this.$el.find('#entries').html('');
      this.$el.addClass('invisible');
    },

    // Search more results
    moreResults: function(event) {
      if(this.model.get('deep')) {
        app.searchView.model.set('deep', this.model.get('deep'));
        app.searchView.search(null, true);
      }
    },

    // Show terms
    showTerms:function(event) {
      var view, request;

      view = new TermsView({
        model: new TermsResponse(),
        el: $(event.target).parent('div')
      });

      $(event.target).remove();

      request = new TermsRequest({
        path: $(event.target).data('path')
      });

       view.model.fetch({
        type: 'post',
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        data: JSON.stringify(request.toJSON()),
      });
    }
  });

  // Export
  return ResultView
});