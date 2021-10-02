package core.commands.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.Interactible;

import java.io.IOException;

public class ExplanationSerializer extends JsonSerializer<Explanation> {

    @Override
    public void serialize(Explanation value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        Interactible explanation = value.explanation();
        String header = explanation.header();
        String usage = explanation.usage();
        gen.writeStringField("header", header);
        gen.writeStringField("usage", usage);
        gen.writeObjectField("options", explanation.options());
        gen.writeEndObject();
    }

}

