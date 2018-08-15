'use strict';

define(['backbone'], function (Backbone) {

  var TermsResponse = Backbone.Model.extend({
    defaults: {
      matches: []
    },
    url: '/terms'
  });

	return TermsResponse;
});