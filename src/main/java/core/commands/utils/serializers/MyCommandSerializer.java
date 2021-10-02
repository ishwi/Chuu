package core.commands.utils.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import core.commands.abstracts.MyCommand;
import core.parsers.explanation.util.Explanation;

import java.io.IOException;
import java.util.List;

public class MyCommandSerializer extends JsonSerializer<MyCommand<?>> {

    @Override
    public void serialize(MyCommand<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        List<Explanation> usages = value.getParser().getUsages();
        gen.writeObjectField("name", value.getName());
        gen.writeObjectField("category", value.getCategory().toString().replaceAll("_", " "));

        gen.writeObjectField("aliases", value.getAliases());
        gen.writeObjectField("parameters", value.getParser().getUsages());
        gen.writeEndObject();
    }

}

