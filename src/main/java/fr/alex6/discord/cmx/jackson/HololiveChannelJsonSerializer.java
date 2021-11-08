package fr.alex6.discord.cmx.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fr.alex6.discord.takobonker.HololiveChannel;

import java.io.IOException;

public class HololiveChannelJsonSerializer extends JsonSerializer<HololiveChannel> {

    @Override
    public void serialize(HololiveChannel hololiveChannel, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (hololiveChannel == null) {
            jsonGenerator.writeNull();
            return;
        }
        jsonGenerator.writeString(hololiveChannel.getId());
    }
}
