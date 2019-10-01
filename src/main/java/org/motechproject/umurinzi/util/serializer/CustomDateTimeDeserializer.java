package org.motechproject.umurinzi.util.serializer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

public class CustomDateTimeDeserializer extends JsonDeserializer<DateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    @Override
    public DateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        DateTime date = null;
        String dateString = parser.getText();

        if (NumberUtils.isNumber(dateString)) {
            date = new DateTime(Long.parseLong(dateString));
        } else if (StringUtils.isNotBlank(dateString)) {
            date = FORMATTER.parseDateTime(dateString);
        }

        return date;
    }
}
