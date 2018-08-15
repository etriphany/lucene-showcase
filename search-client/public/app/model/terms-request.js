'use strict';

define(['backbone'], function (Backbone) {

  var TermsRequest = Backbone.Model.extend({
    defaults: {
      path: ''
    },
  });

	return TermsRequest;
});