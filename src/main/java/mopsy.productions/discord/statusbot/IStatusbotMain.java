package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import okhttp3.OkHttpClient;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.time.Duration;

public interface IStatusbotMain {
    /**
     * The file used as the embed's thumbnail image (e.g. the server's server-icon.png).
     * Return null to disable the thumbnail.
     */
    default File getServerIconFile() {
        return null;
    }

    default void initAll() {
        LogUtils.init(this);
        ConfigManager.init(this);
        DataManager.getAllData(this);
        regDefaultEmbedVarProviders();
    }
    default void addConfigDefaults(YamlFile configuration) {
        ConfigManager.addConfigKey(configuration,"embed_title","Minecraft Server Status",String.join(
                "\n",
                "",
                "The title of the embeds sent by the statusbot",
                "For all possible placeholders, see 'embed_content'"));
        ConfigManager.addConfigKey(configuration,"embed_player_separator_text",", ",
                String.join(
                        "\n",
                        "",
                        "Enter the character(s) displayed between every player name in embeds.",
                        "Changing this to '---' would for example result in:",
                        "playername1---playername2---playername3---playername4"));
    }

    default void sendMessage(String message) {
        if (BotManager.jda == null) {
            System.out.println("JDA uninitialized, skipping message");
            return;
        }

        if (ConfigManager.getBool("enable_text_channel_status_messages")) {
            for (long id : BotManager.messageTextChannels) {
                TextChannel channel = BotManager.jda.getTextChannelById(id);
                if (channel != null)
                    channel.sendMessage(message).queue();
            }
        }

        if (ConfigManager.getBool("enable_direct_message_status_messages")) {
            for (UserChannelPair id : BotManager.messagePrivateChannels) {
                PrivateChannel channel = BotManager.jda.getPrivateChannelById(id.channel);
                if (channel != null)
                    channel.sendMessage(message).queue();
            }
        }
    }

    default void onBotShutdown() {
        if (BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_stop_messages")){
                String stopMessage = Parser.createStopMessage();
                sendMessage(stopMessage);
            }

            EmbedManager.tryUpdateAllEmbeds(this);
        }
        DataManager.saveAllData(this);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (BotManager.jda!=null) {
            BotManager.jda.shutdown();

            try {
                if (!BotManager.jda.awaitShutdown(Duration.ofSeconds(10))) {
                    BotManager.jda.shutdownNow();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            OkHttpClient client = BotManager.jda.getHttpClient();
            client.connectionPool().evictAll();
            client.dispatcher().executorService().shutdownNow();
        }
    }

    default void onBotReady() {
        if (BotManager.jda != null) {
            if (ConfigManager.getBool("enable_server_start_messages")) {
                String startMessage = Parser.createStartMessage();
                sendMessage(startMessage);
            }
        }
    }

    default void onPlayerJoined(String status, String joinMessage, String playerName) {
        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                status,
                this
        );
        if (BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_join_messages")) {
                sendPlayerEventMessage(joinMessage, playerName, true);
            }
        }
    }

    default void onPlayerLeft(String status, String leftMessage, String playerName) {
        BotManager.regBot(
                ConfigManager.configuration.getString("bot_token"),
                status,
                this
        );
        if(BotManager.jda!=null) {
            if (ConfigManager.getBool("enable_server_leave_messages")) {
                sendPlayerEventMessage(leftMessage, playerName, false);
            }
        }
    }

    default void sendPlayerEventMessage(String message, String playerName, boolean isJoin) {
        if (BotManager.jda == null) return;

        net.dv8tion.jda.api.EmbedBuilder builder = new net.dv8tion.jda.api.EmbedBuilder();
        builder.setAuthor(message, null, "https://mc-heads.net/avatar/" + playerName);
        builder.setColor(isJoin ? 0x57F287 : 0xED4245); 

        net.dv8tion.jda.api.entities.MessageEmbed embed = builder.build();

        if (ConfigManager.getBool("enable_text_channel_status_messages")) {
            for (long id : BotManager.messageTextChannels) {
                net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel = BotManager.jda.getTextChannelById(id);
                if (channel != null)
                    channel.sendMessageEmbeds(embed).queue();
            }
        }

        if (ConfigManager.getBool("enable_direct_message_status_messages")) {
            for (UserChannelPair id : BotManager.messagePrivateChannels) {
                net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel channel = BotManager.jda.getPrivateChannelById(id.channel);
                if (channel != null)
                    channel.sendMessageEmbeds(embed).queue();
            }
        }
    }

    default void updateEmbeds() {
        if (BotManager.jda != null) {
            EmbedManager.tryUpdateAllEmbeds(this);
        }
    }

    String getConfigPath();

    void regDefaultEmbedVarProviders();

    default void log(String string, boolean error) {
        if (error)
            System.err.println("Statusbot: " + string);
        else
            System.out.println("Statusbot: " + string);
    }
}
