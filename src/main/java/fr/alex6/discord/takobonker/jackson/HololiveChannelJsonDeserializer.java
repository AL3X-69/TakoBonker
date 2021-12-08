package fr.alex6.discord.takobonker.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import fr.alex6.discord.takobonker.HololiveChannel;

import java.io.IOException;

public class HololiveChannelJsonDeserializer extends JsonDeserializer<HololiveChannel> {
    @Override
    public HololiveChannel deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        try {
            return HololiveChannel.fromId(jsonParser.getValueAsString());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
