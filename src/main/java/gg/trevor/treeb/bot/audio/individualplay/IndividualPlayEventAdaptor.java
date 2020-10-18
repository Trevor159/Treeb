package gg.trevor.treeb.bot.audio.individualplay;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import gg.trevor.treeb.bot.audio.CombinedEventAdapter;
import gg.trevor.treeb.bot.audio.GuildMusicManager;
import gg.trevor.treeb.bot.audio.songguess.SongClip;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class IndividualPlayEventAdaptor extends CombinedEventAdapter
{
	private final SongClip songClip;
	private final Guild guild;
	private final VoiceChannel voiceChannel;

	public IndividualPlayEventAdaptor(
		GuildMusicManager musicManager,
		SongClip songClip,
		Guild guild,
		VoiceChannel voiceChannel
	)
	{
		super(musicManager);
		this.songClip = songClip;
		this.guild = guild;
		this.voiceChannel = voiceChannel;
	}

	@Override
	public void startUp()
	{
		loadAndPlay(songClip);
	}

	@Override
	protected void play(AudioTrack track)
	{
		musicManager.connectToVoiceChannel(guild.getAudioManager(), voiceChannel);

		player.startTrack(track, false);
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
	{
		shutDown();
	}

	@Override
	public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event)
	{
		if (event.getMember().getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong())
		{
			return;
		}
		shutDown();
	}

	@Override
	public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event)
	{
		if (event.getMember().getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong())
		{
			return;
		}
		shutDown();
	}
}
