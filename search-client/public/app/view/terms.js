'use strict';

define([
  'wordcloud',
  'underscore',
  'backbone'
], function (WordCloud, _, Backbone) {

  var TermsView = Backbone.View.extend({

    // Initialize view
    initialize: function() {
      _.bindAll(this, 'render'); // 'this' binder
      this.model.on('change', this.render);
    },

    // Render view
    render: function() {
      // Map model to tag cloud
      var words = _.map(this.model.get('matches'), function(match) {
        return [match.text, match.freq];
      });

      // Render as tag cloud
      this.$el.html("<canvas width='1000' height='400'></canvas>");
      var canvas = this.$el.children()[0];
      WordCloud(canvas, {
        list: words,
        gridSize: 9,
        weightFactor: 7,
        color: 'random-dark',
        rotateRatio: 0.5,
        rotationSteps: 2,
      });

      return this;
    }
  });

	return TermsView;
});