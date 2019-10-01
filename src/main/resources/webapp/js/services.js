(function () {
    'use strict';

    /* Services */

    var services = angular.module('umurinzi.services', ['ngResource']);

    services.factory('AllParticipants', function($resource) {
        return $resource('../umurinzi/participants/all', {}, {});
    });

}());
