package fr.alex6.discord.takobonker;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SuppressWarnings("unused")
public class HololiveChannel {
    // TODO: Change for DB storing
    // EN Myth
    public static final HololiveChannel NINOMAE_INANIS = new HololiveChannel("Ninomae Ina'nis", "UCMwGHR0BTZuLsmjY_NT5Pwg", "\uD83D\uDC19", "", Color.decode("#62567E"));
    public static final HololiveChannel GAWR_GURA = new HololiveChannel("Gawr Gura", "UCoSrY_IQQVpmIRZ9Xf-y93g", "\uD83D\uDD31", "", Color.decode("#5D81C7"));
    public static final HololiveChannel CALLIOPE_MORI = new HololiveChannel("Mori Calliope", "UCL_qhgtOy0dy1Agp8vkySQg", "\uD83D\uDC80", "", Color.decode("#C90D40"));
    public static final HololiveChannel WATSON_AMELIA = new HololiveChannel("Watson Amelia", "UCyl1z3jo3XHR1riLFKG5UAg", "\uD83D\uDD0E", "", Color.decode("#F8DB92"));
    public static final HololiveChannel TAKANASHI_KIARA = new HololiveChannel("Takanashi Kiara", "UCHsx4Hqa-1ORjQTh9TYDhww", "\uD83D\uDC14", "", Color.decode("#FF511C"));
    // EN HOPE
    public static final HololiveChannel IRYS = new HololiveChannel("IRyS", "UC8rcEBzJSleTkf_-agPM20g", "\uD83D\uDC8E", "", Color.decode("#8c1236"));
    // EN Council
    public static final HololiveChannel NANASHI_MUMEI = new HololiveChannel("Nanashi Mumei", "UC3n5uGu18FoCy23ggWWp8tA", "\uD83E\uDEB6", "", Color.decode("#B99C90"));
    public static final HololiveChannel HAKOS_BAELZ = new HololiveChannel("Hakos Baelz", "UCgmPnx-EEeOrZSg5Tiw7ZRQ", "\uD83C\uDFB2", "", Color.decode("#D72517"));
    public static final HololiveChannel CERES_FAUNA = new HololiveChannel("Ceres Fauna", "UCO_aKKYxn4tvrqPjcTzZ6EQ", "\uD83C\uDF3F", "", Color.decode("#ADD198"));
    public static final HololiveChannel OURO_KRONII = new HololiveChannel("Ouro Kronii", "UCmbs8T6MWqUHP1tIQvSgKrg", "⏳", "", Color.decode("#20318B"));
    public static final HololiveChannel TSUKUMO_SANA = new HololiveChannel("Tsukumo Sana", "UCsUj0dszADCGbF3gNrQEuSQ", "\uD83E\uDE90", "", Color.decode("#DEB232"));
    // JP Gen 0
    public static final HololiveChannel TOKINO_SORA = new HololiveChannel("Tokino Sora", "UCp6993wxpyDPHUpavwDFqgg", "", "", Color.decode("#266aff"));
    public static final HololiveChannel ROBOCO = new HololiveChannel("Roboco", "", "UCDqI2jOz0weumE8s7paEk6g", "", Color.decode("#d192fe"));
    public static final HololiveChannel SAKURA_MIKO = new HololiveChannel("Sakura Miko", "UC-hM6YJuNYVAmUWxeIr9FeA", "", "", Color.decode("#ff8fdf"));
    public static final HololiveChannel HOSHIMACHI_SUISEI = new HololiveChannel("Hoshimachi Suisei", "UC5CwaMl1eIgY8h02uZw7u8A", "", "", Color.decode("#49e0f4"));
    // INNK Music
    public static final HololiveChannel AZKi = new HololiveChannel("AZKi", "UC0TXe_LYZ4scaW2XMyi5_kw", "", "", Color.decode("#fc3488"));
    // JP Gen 1
    public static final HololiveChannel YOZORA_MEL = new HololiveChannel("Yozora Mel", "UCD8HOxPs4Xvsm8H0ZxXGiBw", "", "", Color.decode("#fed62a"));
    public static final HololiveChannel SHIRAKAMI_FUBUKI = new HololiveChannel("Shirakami Fubuki", "UCdn5BQ06XqgXoAxIhbqw5Rg", "", "", Color.decode("#43bfef"));
    public static final HololiveChannel NATSUIRO_MATSURI = new HololiveChannel("Natsuiro Matsuri", "UCQ0UDLQCjY0rmuxCDE38FGg", "", "", Color.decode("#fdab45"));
    public static final HololiveChannel AKI_ROSENTHAL = new HololiveChannel("Aki Rosenthal", "UCFTLzh12_nrtzqBPsTCqenA", "", "", Color.decode("#000000")); // Awaiting scheduled stream
    public static final HololiveChannel AKAI_HAATO = new HololiveChannel("Akai Haato", "UC1CfXB_kRs3C-zaeTG3oGyg", "", "", Color.decode("#fe4872"));
    // JP Gen 2
    public static final HololiveChannel MINATO_AQUA = new HololiveChannel("Minato Aqua", "UC1opHUrw8rvnsadT-iGp7Cg", "", "", Color.decode("#e57cdd"));
    public static final HololiveChannel MURASAKI_SHION = new HololiveChannel("Murasaki Shion", "UCXTpFs_3PqI41qX2d9tL2Rw", "", "", Color.decode("#8466fe"));
    public static final HololiveChannel NAKIRI_AYAME = new HololiveChannel("Nakiri Ayame", "UC7fk0CB07ly8oSl0aqKkqFg", "", "", Color.decode("#de3e28"));
    public static final HololiveChannel YUZUKI_CHOCO = new HololiveChannel("Yuzuki Choco", "UC1suqwovbL1kzsoaZgFZLKg", "", "", Color.decode("#fe739e"));
    public static final HololiveChannel OOZORA_SUBARU = new HololiveChannel("Oozora Subaru", "UCvzGlP9oQwU--Y0r9id_jnA", "", "", Color.decode("#e5fb67"));
    // Gamers
    public static final HololiveChannel OOKAMI_MIO = new HololiveChannel("Ookami Mio", "UCp-5t9SrOQwXMU7iIjQfARg", "", "", Color.decode("#c71e3e"));
    public static final HololiveChannel NEKOMATA_OKAYU = new HololiveChannel("Nekomata Okayu", "UCvaTdHTWBGv3MKj3KVqJVCw", "\uD83C\uDF59", "", Color.decode("#b190fc"));
    public static final HololiveChannel INUGAMI_KORONE = new HololiveChannel("Inugami Korone", "UChAnqc_AY5_I3Px5dig3X1Q", "", "", Color.decode("#fee039"));
    // JP Gen 3
    public static final HololiveChannel USADA_PEKORA = new HololiveChannel("Usada Pekora", "UC1DCedRgGHBdm81E1llLhOQ", "", "", Color.decode("#7ec2fe"));
    public static final HololiveChannel URUHA_RUSHIA = new HololiveChannel("Uruha Rushia", "UCl_gCybOJRIgOXw6Qb4qJzQ", "\uD83E\uDD8B", "", Color.decode("#19e8c9"));
    public static final HololiveChannel SHIRANUI_FLARE = new HololiveChannel("Shiranui Flare", "UCvInZx9h3jC2JzsIzoOebWg", "", "", Color.decode("#fe3d1c"));
    public static final HololiveChannel SHIROGANE_NOEL = new HololiveChannel("Shirogane Noel", "UCdyqAaZDKHXg4Ahi7VENThQ", "", "", Color.decode("#acbdc5"));
    public static final HololiveChannel HOUSHOU_MARINE = new HololiveChannel("Houshou Marine", "UCCzUftO8KOVkV4wQG1vkUvg", "", "", Color.decode("#ba2c2b"));
    // JP Gen 4
    public static final HololiveChannel AMANE_KANATA = new HololiveChannel("Amane Kanata", "UCZlDXzGoo7d44bwdNObFacg", "", "", Color.decode("#509bea"));
    public static final HololiveChannel TSUNOKAMI_WATAME = new HololiveChannel("Tsunokami Watame", "UCqm3BQLlJfvkTsX_hvm0UmA", "\uD83D\uDC11", "", Color.decode("#f9afb2"));
    public static final HololiveChannel TOKOYAMI_TOWA = new HololiveChannel("Tokoyami Towa", "UC1uv2Oq6kNxgATlCiez59hw", "", "", Color.decode("#ba92ca"));
    public static final HololiveChannel HIMEMORI_LUNA = new HololiveChannel("Himemori Luna", "UCa9Y57gfeY0Zro_noHRVrnw", "", "", Color.decode("#f7abd5"));
    // JP Gen 5
    public static final HololiveChannel YUKIHANA_LAMI = new HololiveChannel("Yukihana Lami", "UCFKOVgVbGmX65RxO3EtH3iw", "", "", Color.decode("#6abadf"));
    public static final HololiveChannel MOMOSUZU_NENE = new HololiveChannel("Momosuzu Nene", "UCAWSyEs_Io8MtpY3m-zqILA", "", "", Color.decode("#eeac5e"));
    public static final HololiveChannel SHISHIRO_BOTAN = new HololiveChannel("Shishiro Botan", "UCUKD-uaobj9jiqB-VXt71mA", "", "", Color.decode("#a2d5c4"));
    public static final HololiveChannel OMARU_POLKA = new HololiveChannel("Omaru Polka", "UCK9V2B22uJYu3N7eR_BT9QA", "", "", Color.decode("#b92731"));
    // Holostars Gen 1
    public static final HololiveChannel HANASAKI_MIYABI = new HololiveChannel("Hanasaki Miyabi", "UC6t3-_N8A6ME1JShZHHqOMw", "", "", Color.decode("#ff2f3f"));
    public static final HololiveChannel KANADE_IZURU = new HololiveChannel("Kanade Izuru", "UCZgOv3YDEs-ZnZWDYVwJdmA", "", "", Color.decode("#1f2f8f"));
    public static final HololiveChannel ARURANDEISU = new HololiveChannel("Arurandeisu", "UCKeAhJvy8zgXWbh9duVjIaQ", "", "", Color.decode("#2f6f4f"));
    public static final HololiveChannel RIKKAROID = new HololiveChannel("Rikkaroid", "UC9mf_ZVpouoILRY9NUIaK-w", "", "", Color.decode("#ffb7df"));
    // Holostars Gen 2
    public static final HololiveChannel ASTEL_LEDA = new HololiveChannel("Astel Leda", "UCNVEsYbiZjH5QLmGeSgTSzg", "", "", Color.decode("#0047ab"));
    public static final HololiveChannel KISHIDO_TEMA = new HololiveChannel("Kishido Tema", "UCGNI4MENvnsymYjKiZwv9eg", "", "", Color.decode("#000000")); // Awaiting scheduled stream
    public static final HololiveChannel YUKOKU_ROBERU = new HololiveChannel("Yukoku Roberu", "UCANDOlYTJT7N5jlRC3zfzVA", "\uD83C\uDF77", "", Color.decode("#ca5a00"));
    // Holostars Gen 3
    public static final HololiveChannel KAGEYAMA_SHIEN = new HololiveChannel("Kageyama Shien", "UChSvpZYRPh0FvG4SJGSga3g", "", "", Color.decode("#000000")); // Awaiting scheduled stream
    public static final HololiveChannel ARAGAMI_OGA = new HololiveChannel("Aragami Oga", "UCwL7dgTxKo8Y4RFIKWaf8gA", "", "", Color.decode("#d6f05b"));
    // ID Gen 1
    public static final HololiveChannel AYUNDA_RISU = new HololiveChannel("Ayunda Risu", "UCOyYb1c43VlX9rc_lT6NKQw", "", "", Color.decode("#ff4133"));
    public static final HololiveChannel MOONA_HOSHINOVA = new HololiveChannel("Moona Hoshinova", "UCP0BspO_AMEe3aQqqpo89Dg", "", "", Color.decode("#892eff"));
    public static final HololiveChannel AIRANI_LOFIFTEEN = new HololiveChannel("Airani Lofifteen", "UCAoy6rzhSf4ydcYjJw3WoVg", "", "", Color.decode("#ff45d5"));
    // ID Gen 2
    public static final HololiveChannel KUREIJI_OLLIE = new HololiveChannel("Kureiji Ollie", "UCYz_5n-uDuChHtLo7My1HnQ", "", "", Color.decode("#D60E54"));
    public static final HololiveChannel ANYA_MELFISSA = new HololiveChannel("Anya Melfissa", "UC727SQYUvx5pDDGQpTICNWg", "", "", Color.decode("#F2C95C"));
    public static final HololiveChannel PAVIOLIA_REINE = new HololiveChannel("Paviolia Reine", "UChgTyjG-pdNvxxhdsXfHQ5Q", "", "", Color.decode("#0F52BA"));

    public static HololiveChannel fromId(String id) throws IllegalAccessException {
        for (Field field : HololiveChannel.class.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod) && field.getType() == HololiveChannel.class) {
                HololiveChannel channel = (HololiveChannel) field.get(null);
                if (channel.getId().equals(id)) return channel;
            }
        }
        return null;
    }

    public static HololiveChannel fromName(String name) throws IllegalAccessException {
        for (Field field : HololiveChannel.class.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod) && field.getType() == HololiveChannel.class) {
                HololiveChannel channel = (HololiveChannel) field.get(null);
                if (channel.getName().contains(name)) return channel;
            }
        }
        return null;
    }

    private final String name;
    private final String id;
    private final String emoji;
    private final String twitter;
    private final Color color;

    public HololiveChannel(String name, String id, String emoji, String twitter, Color color) {
        this.name = name;
        this.id = id;
        this.emoji = emoji;
        this.twitter = twitter;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getTwitter() {
        return twitter;
    }

    public Color getColor() {
        return color;
    }
}
