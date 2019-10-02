(function () {
    'use strict';

    /* App Module */
    var umurinzi = angular.module('umurinzi', ['umurinzi.controllers', 'umurinzi.directives',
          'motech-dashboard', 'data-services', 'ui.directives']), subjectId, callDetailRecordId, smsRecordId;

    $.ajax({
        url: '../mds/entities/getEntity/Umurinzi/Participant',
        success:  function(data) {
            subjectId = data.id;
        },
        async: false
    });

    $.ajax({
        url: '../mds/entities/getEntity/IVR Module/CallDetailRecord',
        success:  function(data) {
            callDetailRecordId = data.id;
        },
        async: false
    });

    $.ajax({
        url: '../mds/entities/getEntity/SMS Module/SmsRecord',
        success:  function(data) {
            smsRecordId = data.id;
        },
        async: false
    });

    $.ajax({
            url: '../umurinzi/available/umurinziTabs',
            success:  function(data) {
                umurinzi.constant('UMURINZI_AVAILABLE_TABS', data);
            },
            async:    false
        });

    umurinzi.run(function ($rootScope, UMURINZI_AVAILABLE_TABS) {
            $rootScope.UMURINZI_AVAILABLE_TABS = UMURINZI_AVAILABLE_TABS;
        });

    umurinzi.config(function ($routeProvider, UMURINZI_AVAILABLE_TABS) {
        var i, tab;

        for (i = 0; i < UMURINZI_AVAILABLE_TABS.length; i = i + 1) {

            tab = UMURINZI_AVAILABLE_TABS[i];

            if (tab === "subjects") {
                $routeProvider.when('/umurinzi/{0}'.format(tab), {
                    templateUrl: '../umurinzi/resources/partials/umurinziInstances.html',
                    controller: 'MdsDataBrowserCtrl',
                    resolve: {
                        entityId: function ($route) {
                            $route.current.params.entityId = subjectId;
                        },
                        moduleName: function ($route) {
                            $route.current.params.moduleName = 'umurinzi';
                        }
                    }
                });
            } else if (tab === "reports") {
                $routeProvider
                  .when('/umurinzi/reports', { templateUrl: '../umurinzi/resources/partials/reports.html' })
                  .when('/umurinzi/reports/:reportType', { templateUrl: '../umurinzi/resources/partials/report.html', controller: 'UmurinziReportsCtrl' })
                  .when('/umurinzi/callDetailRecord', { redirectTo: '/mds/dataBrowser/' + callDetailRecordId + '/umurinzi' })
                  .when('/umurinzi/SMSLog', { redirectTo: '/mds/dataBrowser/' + smsRecordId + '/umurinzi' });
            } else if (tab === "enrollment") {
                $routeProvider
                  .when('/umurinzi/enrollment', {templateUrl: '../umurinzi/resources/partials/enrollment.html', controller: 'UmurinziEnrollmentCtrl'})
                  .when('/umurinzi/enrollmentAdvanced/:subjectId', {templateUrl: '../umurinzi/resources/partials/enrollmentAdvanced.html', controller: 'UmurinziEnrollmentAdvancedCtrl'});
            } else {
                $routeProvider.when('/umurinzi/{0}'.format(tab),
                    {
                        templateUrl: '../umurinzi/resources/partials/{0}.html'.format(tab),
                        controller: 'Umurinzi{0}Ctrl'.format(tab.capitalize())
                    }
                );
            }
        }

        $routeProvider
            .when('/umurinzi/settings', {templateUrl: '../umurinzi/resources/partials/settings.html', controller: 'UmurinziSettingsCtrl'})
            .when('/umurinzi/welcomeTab', { redirectTo: '/umurinzi/' + UMURINZI_AVAILABLE_TABS[0] });

    });
}());
