package dao.entities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class UserExportSerializer extends StdSerializer<UsersWrapper> {

    protected UserExportSerializer() {
        super(UsersWrapper.class);
    }

    protected UserExportSerializer(Class<UsersWrapper> t) {
        super(t);
    }

    @Override
    public void serialize(UsersWrapper value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("discordUserID", String.valueOf(value.getDiscordID()));
        gen.writeObjectField("lastFMUsername", value.getLastFMName());
        gen.writeEndObject();

    }
}
