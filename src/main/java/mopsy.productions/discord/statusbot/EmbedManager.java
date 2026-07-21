package mopsy.productions.discord.statusbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class EmbedManager {
    public static List<SentEmbedData> sentEmbeds = new ArrayList<>();
    private static final Map<String, Function<IStatusbotMain,String>> varSuppliers = new HashMap<>();
    private static BiFunction<IStatusbotMain,String,String> backupVarSupplier = ((statusbotMain, varName) -> {
        LogUtils.log("Unknown variable: "+varName, true);
        return varName;
    });
    private static Function<IStatusbotMain,Boolean> onlineStatusSupplier = (statusbotMain) -> true;
    private static final int COLOR_ONLINE = 0x57F287;  // Discord green
    private static final int COLOR_OFFLINE = 0xED4245; // Discord red
    private static String lastEmbedDescription = "";
    private static String lastEmbedTitle = "";
    private static boolean lastOnlineState = true;

    public static void regOnlineStatusSupplier(Function<IStatusbotMain,Boolean> supplier){
        onlineStatusSupplier = supplier;
    }

    public static void sendEmbed(IStatusbotMain statusbotMain, MessageChannel messageChannel){
        String title = parseEmbedText(statusbotMain,ConfigManager.getStr("embed_title"));
        String description = parseEmbedText(statusbotMain,ConfigManager.getStr("embed_content"));
        boolean online = onlineStatusSupplier.apply(statusbotMain);
        File iconFile = statusbotMain.getServerIconFile();
        MessageEmbed embed = generateEmbed(title, description, online, iconFile, statusbotMain);
        FileUpload fileUpload = buildIconFileUpload(iconFile);

        MessageCreateAction action = messageChannel.sendMessageEmbeds(embed);
        if (fileUpload != null)
            action = action.addFiles(fileUpload);

        action.queue(e->{
            if (e.getChannel() instanceof PrivateChannel)
                sentEmbeds.add(new SentEmbedData(e.getChannel().getIdLong(), e.getIdLong(), e.getChannel().asPrivateChannel().getUser().getIdLong()));
            else
                sentEmbeds.add(new SentEmbedData(e.getChannel().getIdLong(), e.getIdLong()));
        });
    }

        private static MessageEmbed generateEmbed(String title, String description, boolean online, File iconFile, IStatusbotMain statusbotMain){
        String motdTitle = parseEmbedText(statusbotMain, "$motd$");
        
        if (motdTitle == null || motdTitle.trim().isEmpty() || motdTitle.equals("?")) {
            motdTitle = title; 
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(motdTitle)
                .setColor(online ? COLOR_ONLINE : COLOR_OFFLINE)
                .setTimestamp(Instant.now());

        if (online) {
            String statusLine = parseEmbedText(statusbotMain, "$server-status$ **$server-status-text$**");
            
            String ipText = parseEmbedText(statusbotMain, "$server-ip$:$server-port$");
            String versionText = parseEmbedText(statusbotMain, "$server-version$");
            String playersCount = parseEmbedText(statusbotMain, "$amount-of-players$ / $max-players$ Players");
            String uptimeText = parseEmbedText(statusbotMain, "$uptime$");
            String playerList = parseEmbedText(statusbotMain, "$player-list$");

            builder.setThumbnail("https://mcscans.fi/api/servers/" + ipText + "/icon");

            builder.setDescription(statusLine + "\n\n**IP:** `" + ipText + "`\n**Version:** " + versionText);

            if (playerList == null || playerList.trim().isEmpty()) {
                playerList = "*No players online*";
            } else {
                playerList = "`" + playerList + "`"; 
            }

            builder.addField("Status", playersCount, true);
            builder.addField("Uptime", uptimeText, true);
            builder.addField("Online Players", playerList, false);
        } else {
            String statusLine = parseEmbedText(statusbotMain, "$server-status$ **$server-status-text$**");
            builder.setDescription(statusLine);
        }

        return builder.build();
    }

    private static FileUpload buildIconFileUpload(File iconFile){
        if (iconFile == null || !iconFile.exists() || !iconFile.isFile())
            return null;
        try {
            byte[] data = Files.readAllBytes(iconFile.toPath());
            return FileUpload.fromData(data, "server-icon.png");
        } catch (IOException e) {
            LogUtils.log("Could not read the server icon file at " + iconFile.getAbsolutePath() + ": " + e.getMessage(), true);
            return null;
        }
    }

    public static void tryUpdateAllEmbeds(IStatusbotMain statusbotMain){
        String title = parseEmbedText(statusbotMain,ConfigManager.getStr("embed_title"));
        String description = parseEmbedText(statusbotMain,ConfigManager.getStr("embed_content"));
        boolean online = onlineStatusSupplier.apply(statusbotMain);
        if (!title.equals(lastEmbedTitle) || !description.equals(lastEmbedDescription) || online != lastOnlineState) {
            lastEmbedTitle = title;
            lastEmbedDescription = description;
            lastOnlineState = online;
            updateAllEmbeds(title, description, online, statusbotMain.getServerIconFile(), statusbotMain);
        }
    }
    
    public static void updateAllEmbeds(String title, String description, boolean online, File iconFile, IStatusbotMain statusbotMain) {
        MessageEmbed embed = generateEmbed(title, description, online, iconFile, statusbotMain);
        FileUpload fileUpload = buildIconFileUpload(iconFile);
        for (int i = sentEmbeds.size()-1; i >= 0; i--) {
            SentEmbedData embedData = sentEmbeds.get(i);
            if (embedData.isInPrivateChannel) {
                PrivateChannel channel = BotManager.jda.getPrivateChannelById(embedData.channel);
                if (channel != null) {
                    MessageEditAction action = channel.editMessageEmbedsById(embedData.message, embed);
                    if (fileUpload != null)
                        action = action.setFiles(fileUpload);
                    action.queue(null,
                            new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (exception)->{
                                LogUtils.log("Embed message with ID " + embedData.message + " could not be found!", true);
                                LogUtils.log("Removing the embed from update list", true);
                                sentEmbeds.remove(embedData);
                            }));
                }
                else {
                    LogUtils.log("Private channel with ID " + embedData.channel + " for embed could not be found!", true);
                    LogUtils.log("Removing the embed from update list", true);
                    sentEmbeds.remove(i);
                }
            } else {
                TextChannel channel = BotManager.jda.getChannelById(TextChannel.class, embedData.channel);
                if (channel != null) {
                    MessageEditAction action = channel.editMessageEmbedsById(embedData.message, embed);
                    if (fileUpload != null)
                        action = action.setFiles(fileUpload);
                    action.queue(null,
                            new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (exception)->{
                                LogUtils.log("Embed message with ID " + embedData.message + " could not be found!", true);
                                LogUtils.log("Removing the embed from update list", true);
                                sentEmbeds.remove(embedData);
                            }));
                }
                else {
                    LogUtils.log("Text channel with ID " + embedData.channel + " for embed could not be found!", true);
                    LogUtils.log("Removing the embed from update list", true);
                    sentEmbeds.remove(i);
                }
            }
        }
    }

    public static void regVarSupplier(String varName, Function<IStatusbotMain,String> varSupplier){
        varSuppliers.put(varName,varSupplier);
    }
    public static void regBackupVarSupplier(BiFunction<IStatusbotMain,String,String> supplier){
        backupVarSupplier=supplier;
    }

    public static String parseEmbedText(IStatusbotMain statusbotMain, String inputText){
        String res= "";
        char[] partVarName = null;
        int partVarNameLength = 0;
        boolean readingVarName = false;
        char[] inputChars = inputText.toCharArray();
        for (char c : inputChars) {
            if (c == '$'){
                readingVarName = !readingVarName;
                if(readingVarName){
                    partVarName = new char[inputChars.length-2];
                    partVarNameLength = 0;
                }else{
                    String varName = String.valueOf(partVarName).substring(0,partVarNameLength);
                    Function<IStatusbotMain,String> varSupplier = varSuppliers.get(varName);
                    if (varSupplier!=null){
                        res = res.concat(varSupplier.apply(statusbotMain));
                    }else {
                        res = res.concat(backupVarSupplier.apply(statusbotMain,varName));
                    }
                }
            }else{
                if(readingVarName){
                    partVarName[partVarNameLength] = c;
                    partVarNameLength++;
                }else{
                    res = res.concat(String.valueOf(c));
                }
            }
        }
        return res;
    }
}
