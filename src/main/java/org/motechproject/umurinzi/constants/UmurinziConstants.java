package org.motechproject.umurinzi.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public final class UmurinziConstants {

    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";

    public static final String CSV_EXPORT_FORMAT = "csv";

    public static final String TEXT_CSV_CONTENT = "text/csv";
    public static final String APPLICATION_PDF_CONTENT = "application/pdf";

    public static final DateTimeFormatter SIMPLE_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

    public static final Map<String, Float> REPORT_COLUMN_WIDTHS = new LinkedHashMap<String, Float>() {
        {
            put("Participant Id", 64f); //NO CHECKSTYLE MagicNumber
            put("SMS", 32f);
        }
    };

    public static final String ZETES_IMPORT_EVENT = "zetes_import_event";
    public static final String ZETES_IMPORT_EVENT_START_DATE = "zetes_import_event_start_date";
    public static final String ZETES_IMPORT_EVENT_START_TIME = "00:01";
    public static final String ZETES_IMPORT_EVENT_PERIOD = "30 Minutes";

    public static final String REPORT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DAILY_REPORT_EVENT = "daily_report_event";
    public static final String DAILY_REPORT_EVENT_START_DATE = "daily_report_event_start_date";
    public static final String DAILY_REPORT_EVENT_START_HOUR = "00:01";

    public static final String API_KEY = "api_key";
    public static final String MESSAGE_ID = "message_id";
    public static final String WEBHOOK_URL = "webhook_url";
    public static final String SEND_TO_PHONES = "send_to_phones";
    public static final String SEND_SMS_IF_VOICE_FAILS = "send_sms_if_voice_fails";
    public static final String DETECT_VOICEMAIL = "detect_voicemail_action";
    public static final String RETRY_ATTEMPTS_SHORT = "retry_attempts_short";
    public static final String RETRY_DELAY_SHORT = "retry_delay_short";
    public static final String RETRY_ATTEMPTS_LONG = "retry_attempts_long";
    public static final String RETRY_ATTEMPTS_LONG_DEFAULT = "1";
    public static final String SUBJECT_ID = "participant_id";
    public static final String SUBJECT_PHONE_NUMBER = "subscriber_phone";
    public static final String SUBSCRIBER_DATA = "subscriber_data";
    public static final String PREFERRED_LANGUAGE = "preferred_language";
    public static final String PROPERTY = "property";
    public static final String RECEIVE_VOICE = "receive_voice";
    public static final String RECEIVE_SMS = "receive_sms";
    public static final String HELP_LINE = "help_line";
    public static final String VOICE_SENDER_ID = "voice_sender_id";
    public static final String SMS_SENDER_ID = "sms_sender_id";

    public static final String WITH_HELPLINE = "-with-helpline";

    public static final String IVR_CALL_DETAIL_RECORD_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSS";
    public static final String IVR_CALL_DETAIL_RECORD_MOTECH_TIMESTAMP_FIELD = "motechTimestamp";

    public static final String IVR_CALL_DETAIL_RECORD_STATUS_INITIATED = "INITIATED";
    public static final String IVR_CALL_DETAIL_RECORD_STATUS_FINISHED = "Finished";
    public static final String IVR_CALL_DETAIL_RECORD_STATUS_FAILED = "Failed";
    public static final String IVR_CALL_DETAIL_RECORD_STATUS_SUBMITTED = "Submitted";
    public static final String IVR_CALL_DETAIL_RECORD_STATUS_IN_PROGRESS = "In Progress";
    public static final String IVR_CALL_DETAIL_RECORD_NUMBER_OF_ATTEMPTS = "attempts";
    public static final String IVR_CALL_DETAIL_RECORD_END_TIMESTAMP = "end_timestamp";
    public static final String IVR_CALL_DETAIL_RECORD_START_TIMESTAMP = "start_timestamp";
    public static final String IVR_CALL_DETAIL_RECORD_MESSAGE_SECOND_COMPLETED = "message_seconds_completed";
    public static final String IVR_DELIVERY_LOG_ID = "delivery_log_id";
    public static final String IVR_CALL_DETAIL_RECORD_HANGUP_REASON = "hangup_reason";

    public static final String VOTO_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String ENROLLMENTS_TAB_PERMISSION = "umurinziEnrollmentsTab";
    public static final String REPORTS_TAB_PERMISSION = "umurinziReportsTab";
    public static final String SUBJECTS_TAB_PERMISSION = "umurinziSubjectsTab";
    public static final String MANAGE_ENROLLMENTS_PERMISSION = "umurinziManageEnrollments";
    public static final String IMPORT_SUBJECTS_PERMISSION = "umurinziImportSubjects";
    public static final String MANAGE_MODULE = "manageUmurinzi";
    public static final String MANAGE_HOLIDAYS_PERMISSION = "umurinziManageHolidays";

    public static final String HAS_ENROLLMENTS_TAB_ROLE = "hasRole('" + ENROLLMENTS_TAB_PERMISSION + "')";
    public static final String HAS_REPORTS_TAB_ROLE = "hasRole('" + REPORTS_TAB_PERMISSION + "')";
    public static final String HAS_SUBJECTS_TAB_ROLE = "hasRole('" + SUBJECTS_TAB_PERMISSION + "')";
    public static final String HAS_MANAGE_ENROLLMENTS_ROLE = "hasRole('" + MANAGE_ENROLLMENTS_PERMISSION + "')";
    public static final String HAS_IMPORT_SUBJECTS_ROLE = "hasRole('" + IMPORT_SUBJECTS_PERMISSION + "')";
    public static final String HAS_MANAGE_MODULE_ROLE = "hasRole('" + MANAGE_MODULE + "')";
    public static final String HAS_MANAGE_HOLIDAYS_ROLE = "hasRole('" + MANAGE_HOLIDAYS_PERMISSION + "')";

    public static final String VISIT_RESCHEDULE_NAME = "VisitReschedule";

    public static final String DAILY_CLINIC_VISIT_SCHEDULE_REPORT_NAME = "DailyClinicVisitScheduleReport";
    public static final String FOLLOW_UPS_MISSED_CLINIC_VISITS_REPORT_NAME = "FollowupsMissedClinicVisitsReport";
    public static final String M_AND_E_MISSED_CLINIC_VISITS_REPORT_NAME = "MandEMissedClinicVisitsReport";
    public static final String OPTS_OUT_OF_MOTECH_MESSAGES_REPORT_NAME = "ParticipantsWhoOptOutOfReceivingMotechMessagesReport";
    public static final String IVR_AND_SMS_STATISTIC_REPORT_NAME = "NumberOfTimesParticipantsListenedToEachMessageReport";
    public static final String SUBJECT_ENROLLMENTS_NAME = "ParticipantEnrollments";

    public static final Map<String, String> VISIT_RESCHEDULE_FIELDS_MAP = new LinkedHashMap<String, String>() {
        {
            put("Participant Id", "participantId");
            put("Visit Type", "visitType");
            put("Actual Date", "actualDate");
            put("Planned Date", "plannedDate");
        }
    };

    public static final Map<String, String> DAILY_CLINIC_VISIT_SCHEDULE_REPORT_MAP = new LinkedHashMap<String, String>() {
        {
            put("Planned Visit Date", "dateProjected");
            put("Participant ID",    "subject.subjectId");
            put("Phone Number",      "subject.phoneNumber");
            put("Visit type",        "type");
        }
    };

    public static final Map<String, String> FOLLOW_UPS_MISSED_CLINIC_VISITS_REPORT_MAP = new LinkedHashMap<String, String>() {
        {
            put("Participant ID",           "subject.subjectId");
            put("Visit type",               "type");
            put("Planned Visit Date",       "dateProjected");
            put("No Of Days Exceeded Visit", "noOfDaysExceededVisit");
        }
    };

    public static final Map<String, String> M_AND_E_MISSED_CLINIC_VISITS_REPORT_MAP = new LinkedHashMap<String, String>() {
        {
            put("Participant Id",           "subject.subjectId");
            put("Phone",                    "subject.phoneNumber");
            put("Visit type",               "type");
            put("Planned Visit Date",       "dateProjected");
            put("No Of Days Exceeded Visit", "noOfDaysExceededVisit");
        }
    };

    public static final Map<String, String> OPTS_OUT_OF_MOTECH_MESSAGES_REPORT_MAP = new LinkedHashMap<String, String>() {
        {
            put("Participant Id",           "subject.subjectId");
            put("Date of Unenrollment",     "dateOfUnenrollment");
        }
    };


    public static final Map<String, String> IVR_AND_SMS_STATISTIC_REPORT_MAP = new LinkedHashMap<String, String>() {
        {
            put("Participant Id",           "subject.subjectId");
            put("Phone",                    "subject.phoneNumber");
            put("Message ID",               "messageId");
            put("Sent Date",                "sendDate");
            put("Expected Duration",        "expectedDuration");
            put("Time Listened To",         "timeListenedTo");
            put("Call Length",              "callLength");
            put("Percent Listened",         "messagePercentListened");
            put("Received Date",            "receivedDate");
            put("No. of Attempts",          "numberOfAttempts");
            put("SMS",                      "sms");
            put("SMS Received Date",        "smsReceivedDate");
        }
    };

    public static final Map<String, String> SUBJECT_ENROLLMENTS_MAP = new LinkedHashMap<String, String>() {
        {
            put("Participant Id",           "subject.subjectId");
            put("Status",                   "status");
        }
    };

    public static final List<String> AVAILABLE_LOOKUPS_FOR_SUBJECT_ENROLLMENTS =
        new ArrayList<>(Arrays.asList("Find By Participant Id", "Find By Status"));

    public static final List<String> AVAILABLE_LOOKUPS_FOR_VISIT_RESCHEDULE = new ArrayList<>(Arrays.asList(
            "Find By Visit Planned Date",
            "Find By Visit Actual Date",
            "Find By Participant Id"));

    public static final List<String> AVAILABLE_LOOKUPS_FOR_DAILY_CLINIC_VISIT_SCHEDULE_REPORT =
        new ArrayList<>(Arrays.asList("Find By Planned Visit Date Range",
            "Find By Planned Visit Date Range And Type", "Find By Type", "Find By Participant Id"));

    public static final List<String> AVAILABLE_LOOKUPS_FOR_FOLLOWUPS_MISSED_CLINIC_VISITS_REPORT =
        new ArrayList<>(Arrays.asList("Find By Planned Visit Date Range", "Find By Planned Visit Date Range And Type"));

    public static final List<String> AVAILABLE_LOOKUPS_FOR_M_AND_E_MISSED_CLINIC_VISITS_REPORT =
        new ArrayList<>(Arrays.asList("Find By Participant Id", "Find By Planned Visit Date Range",
            "Find By Planned Visit Date Range And Type"));

    public static final List<String> AVAILABLE_LOOKUPS_FOR_OPTS_OUT_OF_MOTECH_MESSAGES_REPORT =
        new ArrayList<>(Arrays.asList("Find By Participant Id", "Find By Date Of Unenrollment"));

    public static final List<String> AVAILABLE_LOOKUPS_FOR_IVR_AND_SMS_STATISTIC_REPORT =
        new ArrayList<>(Arrays.asList("Find By Participant Id", "Find By Participant Phone Number",
            "Find By Sent Date", "Find By Received Date", "Find By SMS Status",
            "Find By SMS Status And Sent Date", "Find By Message Id And Sent Date"));

    public static final List<String> AVAILABLE_LOOKUPS_FOR_SUBJECTS =
        new ArrayList<>(Arrays.asList("Find By Prime Vaccination Date Range", "Find By Boost Vaccination Date Range",
            "Find By Participant Id", "Find By exact Phone Number", "Find By Visit Type And Actual Date Range"));

    private UmurinziConstants() {
    }
}
