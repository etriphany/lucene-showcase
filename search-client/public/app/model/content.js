'use strict';

define(['backbone'], function (Backbone) {

  var Content = Backbone.Model.extend({
    defaults: {
      id: '',
      path: '',
      language: ''
    }
  });

	return Content;
});