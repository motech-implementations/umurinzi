package org.motechproject.umurinzi.util.serializer;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

public class CustomBooleanDeserializer extends JsonDeserializer<Boolean> {

    @Override
    public Boolean deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
        String value = jp.getText();

        if ("Yes".equalsIgnoreCase(value)) {
            return true;
        }

        if ("No".equalsIgnoreCase(value)) {
            return false;
        }

        return null;
    }
}
