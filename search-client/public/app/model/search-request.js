'use strict';

define(['backbone'], function (Backbone) {

  var SearchRequest = Backbone.Model.extend({
    defaults: {
      query: '',
      detectLanguage: true,
      languages: []
    },

    // Validate model
    validate: function(attrs, options) {
      if (attrs.query.trim() == '') {
        return 'Please inform your search';
      }
    }
  });

	return SearchRequest;
});