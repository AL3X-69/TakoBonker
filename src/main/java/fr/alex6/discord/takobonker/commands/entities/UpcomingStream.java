package fr.alex6.discord.takobonker.commands.entities;

import fr.alex6.discord.takobonker.HololiveChannel;

import java.awt.*;
import java.time.LocalDateTime;

public class UpcomingStream {
    private LocalDateTime time;
    private String name;
    private HololiveChannel channel;
    private Participant[] participants;
    private String link;
    private String thumbnail;
    private boolean live;

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HololiveChannel getChannel() {
        return channel;
    }

    public void setChannel(HololiveChannel channel) {
        this.channel = channel;
    }

    public Participant[] getParticipants() {
        return participants;
    }

    public void setParticipants(Participant[] participants) {
        this.participants = participants;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public static class Participant {
        private String name;
        private HololiveChannel channel;
        private String icon;
        private Color color;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public HololiveChannel getChannel() {
            return channel;
        }

        public void setChannel(HololiveChannel channel) {
            this.channel = channel;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }
    }
}