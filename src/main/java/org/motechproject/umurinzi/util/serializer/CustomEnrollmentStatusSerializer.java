package org.motechproject.umurinzi.util.serializer;

import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.motechproject.umurinzi.domain.enums.EnrollmentStatus;

/**
 * Serializer for EnrollmentStatus representation in UI
 */
public class CustomEnrollmentStatusSerializer extends JsonSerializer<EnrollmentStatus> {

    @Override
    public void serialize(EnrollmentStatus status, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(status.getValue());
    }
}
