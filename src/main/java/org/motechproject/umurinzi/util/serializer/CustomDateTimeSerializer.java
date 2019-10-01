package org.motechproject.umurinzi.util.serializer;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

/**
 * Serializer for DateTime representation in UI
 */
public class CustomDateTimeSerializer extends JsonSerializer<DateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    @Override
    public void serialize(DateTime value, JsonGenerator gen, SerializerProvider arg) throws IOException {
        gen.writeString(FORMATTER.print(value));
    }
}
