package org.motechproject.umurinzi.util.serializer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.type.TypeReference;
import org.motechproject.umurinzi.domain.Visit;

import java.io.IOException;
import java.util.List;

public class CustomVisitListDeserializer extends JsonDeserializer<List<Visit>> {

    @Override
    public List<Visit> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        if (node.isArray()) {
            return oc.readValue(oc.treeAsTokens(node), new TypeReference<List<Visit>>() {
            }); //NO CHECKSTYLE WhitespaceAround
        }

        return null;
    }
}
