package org.motechproject.umurinzi.util.serializer;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.motechproject.umurinzi.domain.enums.VisitType;

import java.io.IOException;

/**
 * Deserializer for VisitType representation in UI
 */
public class CustomVisitTypeDeserializer extends JsonDeserializer<VisitType> {

    @Override
    public VisitType deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        String typeString = parser.getText();
        VisitType visitType = VisitType.getByValue(typeString);
        if (visitType == null) {
            visitType = VisitType.valueOf(typeString);
        }
        return visitType;
    }
}
