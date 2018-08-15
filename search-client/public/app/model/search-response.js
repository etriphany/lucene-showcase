'use strict';

define(['underscore','backbone', 'app/model/content',], function (_, Backbone, Content) {

  var SearchResponse = Backbone.Model.extend({
    defaults: {
      matches: [],
      total: 0,
      deep: '',
      first: true
    },

    url: '/search',

    // Parse model
    parse: function(resp, options) {
      this.total = resp.total;
      this.deep = resp.deep;
      this.first = $('#result-count').length == 0;
      this.matches = [];
      // Build our composite model SearchResponse (1) -> (n) Content
      _.each(resp.matches, function(match) {
        this.matches.push(new Content({
          id: match.id,
          path: match.path,
          language: match.language
        }));
      }, this);

      // Resulting model
      return this;
    }
  });


	return SearchResponse;
});