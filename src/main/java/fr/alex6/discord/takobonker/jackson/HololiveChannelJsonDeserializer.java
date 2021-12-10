/*
 * Copyright 2021 Alex6
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
