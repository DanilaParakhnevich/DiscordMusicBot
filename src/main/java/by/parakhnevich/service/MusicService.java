package by.parakhnevich.service;

import by.parakhnevich.music.GuildMusicManager;
import by.parakhnevich.music.scheduler.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MusicService {
    AudioPlayer audioPlayer;
    AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final Map<String, List<AudioTrack>> rememberedTracks;


    public MusicService() {
        playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        audioPlayer = playerManager.createPlayer();
        audioPlayer.addListener(new TrackScheduler(audioPlayer));
        this.musicManagers = new HashMap<>();
        this.rememberedTracks = new HashMap<>();
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public void search(final TextChannel channel, final String trackName) {
        playerManager.loadItem("ytsearch: " + trackName, new FunctionalResultHandler(null ,
                audioPlaylist -> {
                    channel.sendMessage(creatingNormalTrackList(audioPlaylist)).submit();
                    rememberedTracks.put(channel.getId(), audioPlaylist.getTracks());
                },
                null, null)).isDone();

    }

    public void playAfterSearch (final TextChannel channel, final String number, final Member member) {
        if (!rememberedTracks.containsKey(channel.getId())) {
            channel.sendMessage("From begining u need to use /search command to search list of songs").submit();
            return;
        }
        try {
            int num = Integer.parseInt(number);
            if (num > 18 || num < 1) throw new Exception();
        } catch (Exception e) {
            channel.sendMessage("Bad value (must be > 0 and <19)").submit();
            return;
        }
        loadAndPlay(channel, rememberedTracks.get(channel.getId()).get(Integer.parseInt(number) - 1).getInfo().uri, member);
        rememberedTracks.remove(channel.getId());
    }

    public void loadAndPlay(final TextChannel channel, final String trackUrl, final Member member) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        if (rememberedTracks.get(channel.getId()) == null) {
            channel.sendMessage("Now u should use /search command");
        }
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
                play(channel.getGuild(), musicManager, track, member);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack audioTrack : playlist.getTracks()) {
                    musicManager.scheduler.queue(audioTrack);
                    channel.sendMessage(audioTrack.getInfo().title + " added").submit();
                }
                connectToMembersChannel(channel.getGuild().getAudioManager(), member);
                channel.sendMessage("Playlist added").submit();
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
        rememberedTracks.remove(channel.getId());
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, Member member) {
        connectToMembersChannel(guild.getAudioManager(), member);
        musicManager.scheduler.queue(track);
    }

    public void pause (TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        if (musicManager.scheduler.getPlayer().isPaused()) {
            channel.sendMessage("Already.").queue();
            return;
        }
        musicManager.scheduler.onPlayerPause(musicManager.scheduler.getPlayer());
        channel.sendMessage("Paused.").queue();
    }

    public void resume (TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        if (!musicManager.scheduler.getPlayer().isPaused()) {
            channel.sendMessage("Already.").queue();
            return;
        }
        musicManager.scheduler.onPlayerResume(musicManager.scheduler.getPlayer());
        channel.sendMessage("Resumed.").queue();
    }

    public void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();
        channel.sendMessage("Skipped to next track.").queue();
    }

    public void playingNow(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        channel.sendMessage(musicManager.scheduler.getPlayer().getPlayingTrack().getInfo().title).submit();
    }

    public void setVolume(TextChannel channel, String value) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        try {
            musicManager.scheduler.getPlayer().setVolume(Integer.parseInt(value));
        } catch (Exception e) {
            channel.sendMessage("Bad value : " + value).submit();
            return;
        }
        channel.sendMessage("Volume changed into " + value).submit();
    }


    private static void connectToMembersChannel(AudioManager audioManager, Member member) {
        if (!audioManager.isConnected()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                if (voiceChannel.getMembers().contains(member)) {
                    audioManager.openAudioConnection(voiceChannel);
                    return;
                }
            }
        }
        audioManager.openAudioConnection(audioManager.getGuild().getVoiceChannels().get(0));
    }

    private static String creatingNormalTrackList(AudioPlaylist audioPlaylist) {
        StringBuilder builder = new StringBuilder();
        int number = 1;
        for (String title : audioPlaylist.getTracks()
                     .stream()
                     .map(a -> a.getInfo().title)
                     .collect(Collectors.toList())) {
            builder.append(number++).append(".").append(title).append('\n');
        }
        return builder.toString();
    }
}
