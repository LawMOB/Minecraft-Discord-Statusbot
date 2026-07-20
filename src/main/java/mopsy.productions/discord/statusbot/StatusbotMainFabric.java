package mopsy.productions.discord.statusbot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatusbotMainFabric implements IStatusbotMain, ModInitializer {
    private boolean online = true;
    private MinecraftServer server = null;
    private long startTimeMillis = 0;
    private final Logger LOGGER = LoggerFactory.getLogger("statusbot");

    @Override
    public void addConfigDefaults(YamlFile configuration){
        ConfigManager.addConfigKey(configuration,"embed_title","Minecraft Server Status",String.join(
                "\n",
                "",
                "The title of the embeds sent by the statusbot",
                "For all possible placeholders, see 'embed_content'"));
        ConfigManager.addConfigKey(configuration,"embed_content",String.join(
                        "\n",
                        "$server-status$ **$server-status-text$**",
                        "$server-name$",
                        "IP: $server-ip$",
                        "Uptime: $uptime$",
                        "Players: $amount-of-players$/$max-players$",
                        "$player-list$"),
                String.join(
                        "\n",
                        "",
                        "This is the text displayed below the title of embeds",
                        "Possible placeholders are:",
                        "$server-status$ A red (offline) or green (online) circle telling whether the server is online",
                        "$server-status-text$ The text 'Server is Online' or 'Server is Offline'",
                        "$server-name$ The server name, taken from 'server_name' below",
                        "$server-ip$ The server IP/address (auto-detected when 'auto_detect_server_ip' is true, otherwise taken from 'server_ip' below)",
                        "$server-port$ The port the server is running on",
                        "$amount-of-players$ The number of players currently online on the server",
                        "$max-players$ The maximum number of players that can play on the server",
                        "$uptime$ How long the server has been running since it (re)started",
                        "$update-interval$ The embed refresh interval in seconds, taken from 'embed_update_interval_seconds' below",
                        "$motd$ The message of the day of the server",
                        "$player-list$ A list of player names separated by 'embed_player_separator_text'",
                        "",
                        "Note: the embed also automatically shows a colored side-bar (green/red) and a 'today at HH:MM' timestamp",
                        "for whether the server is online, and a thumbnail image if a 'server-icon.png' file exists in the server's root folder."));
        ConfigManager.addConfigKey(configuration,"embed_player_separator_text","` `", ...
                String.join(
                        "\n",
                        "",
                        "Enter the character(s) displayed between every player name in embeds.",
                        "Changing this to '---' would for example result in:",
                        "playername1---playername2---playername3---playername4"));
        ConfigManager.addConfigKey(configuration,"embed_update_interval_seconds","30",
                String.join(
                        "\n",
                        "",
                        "How often (in seconds) the bot refreshes the status embeds (e.g. the uptime and player list).",
                        "Lower values update more often, but may hit Discord rate limits faster on busy servers."));
        ConfigManager.addConfigKey(configuration,"server_name","My Server",
                String.join(
                        "\n",
                        "",
                        "The name of the server, shown in embeds via the $server-name$ placeholder."));
        ConfigManager.addConfigKey(configuration,"auto_detect_server_ip","true",
                String.join(
                        "\n",
                        "",
                        "Options are true/false",
                        "When true (the default), the bot automatically detects your server's public IP address",
                        "and uses it for the $server-ip$ placeholder, ignoring 'server_ip' below.",
                        "When false, the value entered manually in 'server_ip' below is used instead.",
                        "IMPORTANT: '$server-ip$' is a placeholder that only works inside 'embed_title'/'embed_content'.",
                        "Do NOT type '$server-ip$' as the value of 'server_ip' below, it will just show up as literal text."));
        ConfigManager.addConfigKey(configuration,"server_ip","play.example.com",
                String.join(
                        "\n",
                        "",
                        "The IP/address players use to connect, shown in embeds via the $server-ip$ placeholder.",
                        "Only used when 'auto_detect_server_ip' above is set to false.",
                        "Enter a real address here, e.g. 'play.myserver.com' or '123.45.67.89' - not the placeholder text itself."));
    }

    @Override
    public String getConfigPath(){
        return System.getProperty("user.dir") + File.separator + "config";
    }

    @Override
    public void regDefaultEmbedVarProviders(){
        EmbedManager.regVarSupplier("server-status",(statusbotMain) -> ((StatusbotMainFabric)statusbotMain).online?":green_circle:":":red_circle:");
        EmbedManager.regVarSupplier("server-status-text",(statusbotMain) -> ((StatusbotMainFabric)statusbotMain).online?"Server is Online":"Server is Offline");
        EmbedManager.regVarSupplier("amount-of-players",(statusbotMain -> ((StatusbotMainFabric)statusbotMain).online?String.valueOf(((StatusbotMainFabric)statusbotMain).server.getPlayerCount()):"0"));
        EmbedManager.regVarSupplier("player-list",(statusbotMain -> ((StatusbotMainFabric)statusbotMain).online?String.join(ConfigManager.getStr("embed_player_separator_text"),MakeStringList(((StatusbotMainFabric)statusbotMain).server.getPlayerNames())):""));
        EmbedManager.regVarSupplier("max-players",(statusbotMain -> String.valueOf(((StatusbotMainFabric)statusbotMain).server.getMaxPlayers())));
        EmbedManager.regVarSupplier("motd",(statusbotMain -> String.valueOf(((StatusbotMainFabric)statusbotMain).server.getMotd())));
        EmbedManager.regVarSupplier("server-name",(statusbotMain -> ConfigManager.getStr("server_name")));
        EmbedManager.regVarSupplier("server-ip",(statusbotMain -> IpLookup.getServerIp()));
        EmbedManager.regVarSupplier("server-port",(statusbotMain) -> {
            StatusbotMainFabric main = (StatusbotMainFabric) statusbotMain;
            return main.online && main.server != null ? String.valueOf(main.server.getPort()) : "?";
        });
        EmbedManager.regVarSupplier("uptime",(statusbotMain) -> {
            StatusbotMainFabric main = (StatusbotMainFabric) statusbotMain;
            if (!main.online || main.startTimeMillis == 0)
                return "0s";
            return formatUptime(System.currentTimeMillis() - main.startTimeMillis);
        });
        EmbedManager.regVarSupplier("update-interval",(statusbotMain -> ConfigManager.getStr("embed_update_interval_seconds")));
        EmbedManager.regOnlineStatusSupplier((statusbotMain) -> ((StatusbotMainFabric)statusbotMain).online);
    }

    private static String formatUptime(long elapsedMillis){
        long totalSeconds = Math.max(0, elapsedMillis / 1000);
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (days > 0 || hours > 0) sb.append(hours).append("h ");
        if (days > 0 || hours > 0 || minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString();
    }

    private int getUpdateIntervalTicks(){
        try {
            int seconds = Integer.parseInt(ConfigManager.getStr("embed_update_interval_seconds"));
            return Math.max(20, seconds * 20); // never faster than 1 second/20 ticks
        } catch (NumberFormatException e) {
            return 600; // fall back to 30 seconds
        }
    }

    @Override
    public File getServerIconFile(){
        return new File(System.getProperty("user.dir") + File.separator + "server-icon.png");
    }

    @Override
    public void onInitialize() {
        initAll();

        ServerLifecycleEvents.SERVER_STARTED.register(server->{
            this.server=server;
            this.online=true;
            this.startTimeMillis = System.currentTimeMillis();
            IpLookup.refreshAsync();
            BotManager.regBot(
                    ConfigManager.configuration.getString("bot_token"),
                    Parser.createStatusMessage(()->MakeStringList(server.getPlayerNames()),server.getPlayerNames().length),
                    this
            );
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            online = false;
            IStatusbotMain.super.onBotShutdown();
        });

        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler,packetSender,server)->{
            String status = Parser.createStatusMessage(()->MakeStringListWith(server.getPlayerNames(),serverPlayNetworkHandler.getPlayer().getScoreboardName()),server.getPlayerNames().length+1);
            String joinMessage = Parser.createJoinMessage(
                    ()->MakeStringListWith(server.getPlayerNames(),serverPlayNetworkHandler.getPlayer().getScoreboardName()),
                    serverPlayNetworkHandler.getPlayer().getScoreboardName(),
                    server.getPlayerNames().length+1
            );
            IStatusbotMain.super.onPlayerJoined(status, joinMessage);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler,server)->{
            String status = Parser.createStatusMessage(()->MakeStringList(server.getPlayerNames(),serverPlayNetworkHandler.getPlayer().getScoreboardName()),server.getPlayerNames().length-1);
            String leaveMessage = Parser.createLeaveMessage(
                    ()->MakeStringList(server.getPlayerNames(),serverPlayNetworkHandler.getPlayer().getScoreboardName()),
                    serverPlayNetworkHandler.getPlayer().getScoreboardName(),
                    server.getPlayerNames().length-1
            );
            IStatusbotMain.super.onPlayerLeft(status, leaveMessage);
        });

        ServerTickEvents.END_SERVER_TICK.register((server)->{
            if (server.getTickCount() % getUpdateIntervalTicks() == 0)
                IStatusbotMain.super.updateEmbeds();
        });
    }

    private List<String> MakeStringList(String[] players){
        return Arrays.asList(players);
    }

    private List<String> MakeStringList(String[] players, String excluded){
        List<String> res = new ArrayList<>(players.length-1);
        for(String player : players){
            if(!player.equals(excluded)){
                res.add(player);
            }
        }
        return res;
    }
    private List<String> MakeStringListWith(String[] players, String extra) {
        List<String> res = new ArrayList<>(players.length + 1);
        res.addAll(Arrays.asList(players));
        res.add(extra);
        return res;
    }

    @Override
    public void log(String string, boolean error) {
        if (error)
            LOGGER.error(string);
        else
            LOGGER.info(string);
    }
}
