/*
 * Copyright 2022 Alex6
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

package fr.alex6.takobonker.bot;

import fr.alex6.takobonker.api.http.HttpFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class ScheduleEmbedTask extends TimerTask {
    private final HttpFactory httpFactory = HttpFactory.SCHEDULE_HOLOLIVE_TV;
    private final JDA jda;

    public ScheduleEmbedTask(JDA jda) {
        this.jda = jda;
    }

    private TextChannel getScheduleChannel() {
        return jda.getTextChannelById("890913396218822658");
    }

    private Message getScheduleMessage() {
        return getScheduleChannel().retrieveMessageById("").complete();
    }

    private List<PlannedStream> getPlannedStreams() throws IOException {
        List<PlannedStream> plannedStreams = new ArrayList<>();
        Elements elements = httpFactory.getHtmlDocument("/english").getElementsByClass("thumbnail");
        for (Element element : elements) {
            PlannedStream plannedStream = new PlannedStream(
                    "",
                    "",
                    getUsernameByNickname(element.getElementsByClass("name").get(0).text()),
                    new String[]{},
                    "",
                    null
            ); // TODO: Finish
            plannedStreams.add(plannedStream);
        }
        return plannedStreams;
    }

    private String getUsernameByNickname(String name) {
        name = name.toLowerCase(Locale.ROOT);
        switch (name) {
            case "ina":
                return "Ninomae Ina'nis";
            case "gura":
                return "Gawr Gura";
            case "calli":
                return "Calliope Mori";
            case "kiara":
                return "Takanashi Kiara";
            case "amelia":
                return "Amelia Watson";
            case "irys":
                return "IRyS";
            case "mumei":
                return "Nanashi Mumei";
            case "fauna":
                return "Ceres Fauna";
            case "kronii":
                return "Ouro Kronii";
            case "baelz":
                return "Hakos Baelz";
            case "sana":
                return "Tsukomo Sana";
            default:
                return null;
        }
    }

    @Override
    public void run() {

    }

    private static class PlannedStream {
        private final String channelId;
        private final String authorId;
        private final String author;
        private final String[] participants;
        private final String thumbnail;
        private final Date date;

        public PlannedStream(String channelId, String authorId, String author, String[] participants, String thumbnail, Date date) {
            this.channelId = channelId;
            this.authorId = authorId;
            this.author = author;
            this.participants = participants;
            this.thumbnail = thumbnail;
            this.date = date;
        }

        public String getChannelId() {
            return channelId;
        }

        public String getAuthorId() {
            return authorId;
        }

        public String getAuthor() {
            return author;
        }

        public String[] getParticipants() {
            return participants;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public Date getDate() {
            return date;
        }
    }
}
