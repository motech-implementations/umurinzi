package org.motechproject.umurinzi.util.serializer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

/**
 * Serializer for Double representation in UI
 */
public class CustomDoubleSerializer extends JsonSerializer<Double> {

    private static final DecimalFormat FORMATTER = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider arg)
            throws IOException {
        gen.writeString(FORMATTER.format(value));
    }
}
