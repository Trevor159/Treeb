package gg.trevor.treeb.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import gg.trevor.treeb.bot.Bot;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class AudioHandler extends ListenerAdapter
{
	@Autowired
	private Bot bot;

	private final AudioPlayerManager playerManager;
	private final Map<Long, GuildMusicManager> musicManagers;

	public AudioHandler()
	{
		this.musicManagers = new HashMap<>();

		this.playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}

	public synchronized GuildMusicManager getOrCreateGuildAudioPlayer(Guild guild)
	{
		long guildId = Long.parseLong(guild.getId());
		GuildMusicManager musicManager = musicManagers.get(guildId);

		if (musicManager == null)
		{
			musicManager = new GuildMusicManager(playerManager);
			musicManagers.put(guildId, musicManager);
		}

		guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(musicManager.getPlayer()));

		return musicManager;
	}

	synchronized GuildMusicManager getGuildAudioPlayer(Guild guild)
	{
		long guildId = Long.parseLong(guild.getId());
		return musicManagers.get(guildId);
	}

	@Override
	public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event)
	{
		CombinedEventAdapter eventAdapter = getEventAdaptor(event);

		if (eventAdapter != null)
		{
			eventAdapter.onGuildVoiceLeave(event);
		}
	}

	@Override
	public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event)
	{
		CombinedEventAdapter eventAdapter = getEventAdaptor(event);

		if (eventAdapter != null)
		{
			eventAdapter.onGuildVoiceMove(event);
		}
	}

	@Override
	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
	{
		CombinedEventAdapter eventAdapter = getEventAdaptor(event);

		if (eventAdapter != null)
		{
			eventAdapter.onGuildMessageReceived(event);
		}
	}

	private CombinedEventAdapter getEventAdaptor(GenericGuildEvent event)
	{
		GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());

		if (musicManager == null || musicManager.getEventAdapter() == null)
		{
			return null;
		}

		return musicManager.getEventAdapter();
	}

	@Scheduled(
		fixedDelay = 60 * 1000,
		initialDelay = 60 * 1000
	)
	private synchronized void checkInactivity()
	{
		for (Long guildId : musicManagers.keySet())
		{
			GuildMusicManager musicManager = musicManagers.get(guildId);
			if (musicManager.isInactive())
			{
				Guild guild = bot.getJda().getGuildById(guildId);

				if (guild == null)
				{
					log.error("Somehow guild is null");
					continue;
				}

				musicManager.disconnectFromVoiceChannel(guild.getAudioManager());

				musicManagers.remove(guildId);
			}
		}
	}
}
