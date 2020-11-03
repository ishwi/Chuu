package core.commands.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import core.commands.MyCommand;

import java.io.IOException;

public class MyCommandSerializer extends JsonSerializer<MyCommand<?>> {

    @Override
    public void serialize(MyCommand<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("name", value.getName());
        gen.writeObjectField("category", value.getCategory().toString().replaceAll("_", " "));
        gen.writeObjectField("aliases", value.getAliases());
        gen.writeObjectField("instructions", value.getParser().getUsageLogic(value.getAliases().get(0)));
        gen.writeEndObject();
    }

}

