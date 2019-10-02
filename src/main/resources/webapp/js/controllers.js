(function() {
    'use strict';

    var controllers = angular.module('umurinzi.controllers', []);

    controllers.controller('UmurinziBaseCtrl', function ($scope, $timeout, $http, MDSUtils) {

        $scope.filters = [{
            name: $scope.msg('umurinzi.today'),
            dateFilter: "TODAY"
        },{
            name: $scope.msg('umurinzi.tomorrow'),
            dateFilter: "TOMORROW"
        },{
            name: $scope.msg('umurinzi.twoDaysAfter'),
            dateFilter: "TWO_DAYS_AFTER"
        },{
            name: $scope.msg('umurinzi.nextThreeDays'),
            dateFilter: "NEXT_THREE_DAYS"
        },{
            name: $scope.msg('umurinzi.thisWeek'),
            dateFilter: "THIS_WEEK"
        },{
            name: $scope.msg('umurinzi.dateRange'),
            dateFilter: "DATE_RANGE"
        }];

        $scope.selectedFilter = $scope.filters[0];

        $scope.selectFilter = function(value) {
            $scope.selectedFilter = $scope.filters[value];
            if (value !== 5) {
                $scope.refreshGrid();
            }
        };

        $scope.cardDateFormat = "dd-MM-yyyy";
        $scope.cardDateTimeFormat = "dd-MM-yyyy HH:mm";

        $scope.availableExportRecords = ['All','10', '25', '50', '100', '250'];
        $scope.availableExportFormats = ['pdf','xls'];
        $scope.actualExportRecords = 'All';
        $scope.actualExportColumns = 'All';
        $scope.exportFormat = 'pdf';
        $scope.checkboxModel = {
            exportWithOrder : false,
            exportWithFilter : true
        };

        $scope.exportEntityInstances = function () {
            $scope.checkboxModel.exportWithFilter = true;
            $('#exportUmurinziInstanceModal').modal('show');
        };

        $scope.changeExportRecords = function (records) {
            $scope.actualExportRecords = records;
        };

        $scope.changeExportFormat = function (format) {
            $scope.exportFormat = format;
        };

        $scope.closeExportUmurinziInstanceModal = function () {
            $('#exportUmurinziInstanceModal').resetForm();
            $('#exportUmurinziInstanceModal').modal('hide');
        };

        $scope.exportInstanceWithUrl = function(url) {
            if ($scope.selectedLookup !== undefined && $scope.checkboxModel.exportWithFilter === true) {
                url = url + "&lookup=" + (($scope.selectedLookup) ? $scope.selectedLookup.lookupName : "");
                url = url + "&fields=" + encodeURIComponent(JSON.stringify($scope.lookupBy));
            }

            $http.get(url)
            .success(function () {
                $('#exportUmurinziInstanceModal').resetForm();
                $('#exportUmurinziInstanceModal').modal('hide');
                window.location.replace(url);
            })
            .error(function (response) {
                handleResponse('mds.error', 'mds.error.exportData', response);
            });
        };

        $scope.parseDate = function(date, offset) {
            if (date !== undefined && date !== null) {
                var parts = date.split('-'), date;

                if (offset) {
                    date = new Date(parts[0], parts[1] - 1, parseInt(parts[2]) + offset);
                } else {
                    date = new Date(parts[0], parts[1] - 1, parts[2]);
                }
                return date;
            }
            return undefined;
        };

        $scope.lookupBy = {};
        $scope.selectedLookup = undefined;
        $scope.lookupFields = [];

        $scope.getLookups = function(url) {
            $scope.lookupBy = {};
            $scope.selectedLookup = undefined;
            $scope.lookupFields = [];

            $http.get(url)
            .success(function(data) {
                $scope.lookups = data;
            });
        };

        /**
        * Shows/Hides lookup dialog
        */
        $scope.showLookupDialog = function() {
            $("#lookup-dialog")
            .css({'top': ($("#lookupDialogButton").offset().top - $("#main-content").offset().top) - 40,
            'left': ($("#lookupDialogButton").offset().left - $("#main-content").offset().left) - 15})
            .toggle();
            $("div.arrow").css({'left': 50});
        };

        $scope.hideLookupDialog = function() {
            $("#lookup-dialog").hide();
        };

        /**
        * Marks passed lookup as selected. Sets fields that belong to the given lookup and resets lookupBy object
        * used to filter instances by given values
        */
        $scope.selectLookup = function(lookup) {
            $scope.selectedLookup = lookup;
            $scope.lookupFields = lookup.lookupFields;
            $scope.lookupBy = {};
        };

        /**
        * Removes lookup and resets all fields associated with a lookup
        */
        $scope.removeLookup = function() {
            $scope.lookupBy = {};
            $scope.selectedLookup = undefined;
            $scope.lookupFields = [];
            $scope.filterInstancesByLookup();
        };

        /**
        * Hides lookup dialog and sends signal to refresh the grid with new data
        */
        $scope.filterInstancesByLookup = function() {
            $scope.showLookupDialog();
            $scope.refreshGrid();
        };

        $scope.refreshGrid = function() {
            $scope.lookupRefresh = !$scope.lookupRefresh;
        };

        $scope.buildLookupFieldName = function (field) {
            if (field.relatedName !== undefined && field.relatedName !== '' && field.relatedName !== null) {
                return field.name + "." + field.relatedName;
            }
            return field.name;
        };

        /**
        * Depending on the field type, includes proper html file containing visual representation for
        * the object type. Radio input for boolean, select input for list and text input as default one.
        */
        $scope.loadInputForLookupField = function(field) {
            var value = "default", type = "field";

            if (field.className === "java.lang.Boolean") {
                value = "boolean";
            } else if (field.className === "java.util.Collection") {
                value = "list";
            } else if (field.className === "org.joda.time.DateTime" || field.className === "java.util.Date") {
                value = "datetime";
            } else if (field.className === "org.joda.time.LocalDate") {
                value = "date";
            }

            if ($scope.isRangedLookup(field)) {
                type = "range";
                if (!$scope.lookupBy[$scope.buildLookupFieldName(field)]) {
                    $scope.lookupBy[$scope.buildLookupFieldName(field)] = {min: '', max: ''};
                }
            } else if ($scope.isSetLookup(field)) {
                type = 'set';
                if (!$scope.lookupBy[$scope.buildLookupFieldName(field)]) {
                    $scope.lookupBy[$scope.buildLookupFieldName(field)] = [];
                }
            }

            return '../umurinzi/resources/partials/lookups/{0}-{1}.html'.format(type, value);
        };

        $scope.isRangedLookup = function(field) {
            return $scope.isLookupFieldOfType(field, 'RANGE');
        };

        $scope.isSetLookup = function(field) {
            return $scope.isLookupFieldOfType(field, 'SET');
        };

        $scope.isLookupFieldOfType = function(field, type) {
            var i, lookupField;
            for (i = 0; i < $scope.selectedLookup.lookupFields.length; i += 1) {
                lookupField = $scope.selectedLookup.lookupFields[i];
                if ($scope.buildLookupFieldName(lookupField) === $scope.buildLookupFieldName(field)) {
                    return lookupField.type === type;
                }
            }
        };

        $scope.getComboboxValues = function (settings) {
            var labelValues = MDSUtils.find(settings, [{field: 'name', value: 'mds.form.label.values'}], true).value, keys = [], key;
            // Check the user supplied flag, if true return string set
            if (MDSUtils.find(settings, [{field: 'name', value: 'mds.form.label.allowUserSupplied'}], true).value === true){
                return labelValues;
            } else {
                if (labelValues !== undefined && labelValues[0].indexOf(":") !== -1) {
                    labelValues =  $scope.getAndSplitComboboxValues(labelValues);
                    for(key in labelValues) {
                        keys.push(key);
                    }
                    return keys;
                } else {        // there is no colon, so we are dealing with a string set, not a map
                    return labelValues;
                }
            }
        };

        $scope.getComboboxDisplayName = function (settings, value) {
            var labelValues = MDSUtils.find(settings, [{field: 'name', value: 'mds.form.label.values'}], true).value;
            // Check the user supplied flag, if true return string set
            if (MDSUtils.find(settings, [{field: 'name', value: 'mds.form.label.allowUserSupplied'}], true).value === true){
                return value;
            } else {
                if (labelValues[0].indexOf(":") === -1) { // there is no colon, so we are dealing with a string set, not a map
                    return value;
                } else {
                    labelValues =  $scope.getAndSplitComboboxValues(labelValues);
                    return labelValues[value];
                }
            }

        };

        $scope.getAndSplitComboboxValues = function (labelValues) {
            var doublet, i, map = {};
            for (i = 0; i < labelValues.length; i += 1) {
                doublet = labelValues[i].split(":");
                map[doublet[0]] = doublet[1];
            }
            return map;
        };

        $scope.resizeGridHeight = function(gridId) {
            var intervalHeightResize, gap, tableHeight;
            clearInterval(intervalHeightResize);
            intervalHeightResize = setInterval( function () {
                if ($('.overrideJqgridTable').offset() !== undefined) {
                    gap = 1 + $('.overrideJqgridTable').offset().top - $('.inner-center .ui-layout-content').offset().top;
                    tableHeight = Math.floor($('.inner-center .ui-layout-content').height() - gap - $('.ui-jqgrid-pager').outerHeight() - $('.ui-jqgrid-hdiv').outerHeight());
                    $('#' + gridId).jqGrid("setGridHeight", tableHeight);
                }
                clearInterval(intervalHeightResize);
            }, 250);
         };

        $scope.resizeGridWidth = function(gridId) {
            var intervalWidthResize, tableWidth;
            clearInterval(intervalWidthResize);
            intervalWidthResize = setInterval( function () {
                tableWidth = $('.overrideJqgridTable').width();
                $('#' + gridId).jqGrid("setGridWidth", tableWidth);
                clearInterval(intervalWidthResize);
            }, 250);
        }
    });

    controllers.controller('UmurinziSettingsCtrl', function ($scope, $http, $timeout) {
        $scope.errors = [];
        $scope.messages = [];

        $http.get('../umurinzi/umurinzi-config')
            .success(function(response){
                var i;
                $scope.config = response;
                $scope.originalConfig = angular.copy($scope.config);
            })
            .error(function(response) {
                $scope.errors.push($scope.msg('umurinzi.settings.noConfig', response));
            });

        $scope.reset = function () {
            $scope.config = angular.copy($scope.originalConfig);
        };

        function hideMsgLater(index) {
            return $timeout(function() {
                $scope.messages.splice(index, 1);
            }, 5000);
        }

        $scope.submit = function () {
            $http.post('../umurinzi/umurinzi-config', $scope.config)
                .success(function (response) {
                    $scope.config = response;
                    $scope.originalConfig = angular.copy($scope.config);
                    var index = $scope.messages.push($scope.msg('umurinzi.settings.saved'));
                    hideMsgLater(index-1);
                })
                .error (function (response) {
                    //todo: better than that!
                    handleWithStackTrace('umurinzi.error.header', 'umurinzi.error.body', response);
                });
        };
    });

    controllers.controller('UmurinziReportsCtrl', function ($scope, $routeParams) {
        $scope.reportType = $routeParams.reportType;
        $scope.reportName = "";

        $scope.$parent.selectedFilter.startDate = undefined;
        $scope.$parent.selectedFilter.endDate = undefined;
        $scope.$parent.selectedFilter = $scope.filters[0];

        $scope.buildColumnModel = function (colModel) {
            var newColModel = colModel;
            for (var i in colModel) {
                if(!colModel[i].hasOwnProperty('formatoptions') && colModel[i].hasOwnProperty('formatter')) {
                    newColModel[i].formatter = eval("(" + colModel[i].formatter + ")");
                }
            }
            return newColModel;
        };

        $scope.buildColumnNames = function (colNames) {
            var newColNames = colNames;
            for(var i in colNames) {
                newColNames[i] = $scope.msg(colNames[i]);
            }
            return newColNames;
        };

        var url;
        switch($scope.reportType){
            case "dailyClinicVisitScheduleReport":
                url = "../umurinzi/getLookupsForDailyClinicVisitScheduleReport";
                $scope.reportName = $scope.msg('umurinzi.reports.dailyClinicVisitScheduleReport');
                break;
            case "followupsMissedClinicVisitsReport":
                url = "../umurinzi/getLookupsForFollowupsMissedClinicVisitsReport";
                $scope.reportName = $scope.msg('umurinzi.reports.followupsMissedClinicVisitsReport');
                break;
            case "MandEMissedClinicVisitsReport":
                url = "../umurinzi/getLookupsForMandEMissedClinicVisitsReport";
                $scope.reportName = $scope.msg('umurinzi.reports.MandEMissedClinicVisitsReport');
                break;
            case "ivrAndSmsStatisticReport":
                url = "../umurinzi/getLookupsForIvrAndSmsStatisticReport";
                $scope.reportName = $scope.msg('umurinzi.reports.ivrAndSmsStatisticReport');
                break;
            case "optsOutOfMotechMessagesReport":
                url = "../umurinzi/getLookupsForOptsOutOfMotechMessagesReport";
                $scope.reportName = $scope.msg('umurinzi.reports.optsOutOfMotechMessagesReport');
                break;
        }
        $scope.getLookups(url);

        $scope.exportInstance = function() {
            var url, rows, page, sortColumn, sortDirection;

            switch($scope.reportType){
                case "dailyClinicVisitScheduleReport":
                    url = "../umurinzi/exportDailyClinicVisitScheduleReport";
                    break;
                case "followupsMissedClinicVisitsReport":
                    url = "../umurinzi/exportFollowupsMissedClinicVisitsReport";
                    break;
                case "MandEMissedClinicVisitsReport":
                    url = "../umurinzi/exportMandEMissedClinicVisitsReport";
                    break;
                case "optsOutOfMotechMessagesReport":
                    url = "../umurinzi/exportOptsOutOfMotechMessagesReport";
                    break;
                case "ivrAndSmsStatisticReport":
                    url = "../umurinzi/exportIvrAndSmsStatisticReport";
                    break;
            }
            url = url + "?outputFormat=" + $scope.exportFormat;
            url = url + "&exportRecords=" + $scope.actualExportRecords;

            if ($scope.checkboxModel.exportWithOrder === true) {
                sortColumn = $('#reportTable').getGridParam('sortname');
                sortDirection = $('#reportTable').getGridParam('sortorder');

                url = url + "&sortColumn=" + sortColumn;
                url = url + "&sortDirection=" + sortDirection;
            }

            if ($scope.checkboxModel.exportWithFilter === true) {
                url = url + "&dateFilter=" + $scope.selectedFilter.dateFilter;

                if ($scope.selectedFilter.startDate) {
                    url = url + "&startDate=" + $scope.selectedFilter.startDate;
                }

                if ($scope.selectedFilter.endDate) {
                    url = url + "&endDate=" + $scope.selectedFilter.endDate;
                }
            }

            $scope.exportInstanceWithUrl(url);
        };

        $scope.backToEntityList = function() {
            window.location.replace('#/umurinzi/reports');
        };
    });

    controllers.controller('UmurinziEnrollmentCtrl', function ($scope, $http) {
        var url = "../umurinzi/getLookupsForEnrollments";
        $scope.getLookups(url);

        $scope.enrollInProgress = false;

        $scope.refreshGrid = function() {
            $scope.lookupRefresh = !$scope.lookupRefresh;
        };

        $scope.refreshGridAndStayOnSamePage = function() {
            $scope.gridRefresh = !$scope.gridRefresh;
        };

        $scope.enroll = function(subjectId) {
            motechConfirm("umurinzi.enrollSubject.ConfirmMsg", "umurinzi.enrollSubject.ConfirmTitle",
              function (response) {
                  if (!response) {
                      return;
                  } else {
                      $scope.enrollInProgress = true;
                      $http.post('../umurinzi/enrollSubject', subjectId)
                        .success(function(response) {
                            motechAlert('umurinzi.enrollment.enrollSubject.success', 'umurinzi.enrollment.enrolledSubject');
                            $scope.refreshGridAndStayOnSamePage();
                            $scope.enrollInProgress = false;
                        })
                        .error(function(response) {
                            motechAlert('umurinzi.enrollment.enrollSubject.error', 'umurinzi.enrollment.error', response);
                            $scope.refreshGridAndStayOnSamePage();
                            $scope.enrollInProgress = false;
                        });
                  }
              });
        };

        $scope.unenroll = function(subjectId) {
            motechConfirm("umurinzi.unenrollSubject.ConfirmMsg", "umurinzi.unenrollSubject.ConfirmTitle",
              function (response) {
                  if (!response) {
                      return;
                  } else {
                      $scope.enrollInProgress = true;
                      $http.post('../umurinzi/unenrollSubject', subjectId)
                        .success(function(response) {
                            motechAlert('umurinzi.enrollment.unenrollSubject.success', 'umurinzi.enrollment.unenrolledSubject');
                            $scope.refreshGridAndStayOnSamePage();
                            $scope.enrollInProgress = false;
                        })
                        .error(function(response) {
                            motechAlert('umurinzi.enrollment.unenrollSubject.error', 'umurinzi.enrollment.error', response);
                            $scope.refreshGridAndStayOnSamePage();
                            $scope.enrollInProgress = false;
                        });
                  }
              });
        };

        $scope.goToAdvanced = function(subjectId) {
            $.ajax({
                url: '../umurinzi/checkAdvancedPermissions',
                success:  function(data) {
                    window.location.replace('#/umurinzi/enrollmentAdvanced/' + subjectId);
                },
                async: false
            });
        };

        $scope.exportInstance = function() {
            var url, rows, page, sortColumn, sortDirection;

            url = "../umurinzi/exportSubjectEnrollment";
            url = url + "?outputFormat=" + $scope.exportFormat;
            url = url + "&exportRecords=" + $scope.actualExportRecords;

            if ($scope.checkboxModel.exportWithOrder === true) {
                sortColumn = $('#enrollmentTable').getGridParam('sortname');
                sortDirection = $('#enrollmentTable').getGridParam('sortorder');

                url = url + "&sortColumn=" + sortColumn;
                url = url + "&sortDirection=" + sortDirection;
            }

            $scope.exportInstanceWithUrl(url);
        };
    });

    controllers.controller('UmurinziEnrollmentAdvancedCtrl', function ($scope, $http, $timeout, $routeParams) {
        $scope.enrollInProgress = false;

        $scope.backToEnrolments = function() {
            window.location.replace('#/umurinzi/enrollment');
        };

        $scope.selectedSubjectId = $routeParams.subjectId;

        $scope.refreshGrid = function() {
            $scope.lookupRefresh = !$scope.lookupRefresh;
        };

        $scope.enroll = function(campaignName) {
            $scope.enrollInProgress = true;
            $http.get('../umurinzi/enrollCampaign/' + $scope.selectedSubjectId + '/' + campaignName)
              .success(function(response) {
                  motechAlert('umurinzi.enrollment.enrollSubject.success', 'umurinzi.enrollment.enrolledSubject');
                  $scope.refreshGrid();
                  $scope.enrollInProgress = false;
              })
              .error(function(response) {
                  motechAlert('umurinzi.enrollment.enrollSubject.error', 'umurinzi.enrollment.error', response);
                  $scope.refreshGrid();
                  $scope.enrollInProgress = false;
              });
        };

        $scope.unenroll = function(campaignName) {
            $scope.enrollInProgress = true;
            $http.get('../umurinzi/unenrollCampaign/' + $scope.selectedSubjectId + '/' + campaignName)
              .success(function(response) {
                  motechAlert('umurinzi.enrollment.unenrollSubject.success', 'umurinzi.enrollment.unenrolledSubject');
                  $scope.refreshGrid();
                  $scope.enrollInProgress = false;
              })
              .error(function(response) {
                  motechAlert('umurinzi.enrollment.unenrollSubject.error', 'umurinzi.enrollment.error', response);
                  $scope.refreshGrid();
                  $scope.enrollInProgress = false;
              });
        };
    });

}());
