package gg.trevor.treeb.bot.audio.songguess;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import gg.trevor.treeb.Util;
import gg.trevor.treeb.bot.audio.CombinedEventAdapter;
import gg.trevor.treeb.bot.audio.GuildMusicManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class SongGuessEventAdaptor extends CombinedEventAdapter
{
	private final ScheduledExecutorService executorService;
	private final List<SongClip> songClips;
	private final JDA jda;
	private final Long voiceChannelId;
	private final Long guildId;
	private final Long textChannelId;


	private int currentIndex;
	private int lives;
	private SongAnswer songAnswer;
	private Map<Long, Double> scores = new HashMap<>();

	public SongGuessEventAdaptor(
		GuildMusicManager musicManager,
		ScheduledExecutorService executorService,
		List<SongClip> songClips,
		JDA jda,
		VoiceChannel voiceChannel,
		Guild guild,
		TextChannel textChannel
	)
	{
		super(musicManager);
		this.executorService = executorService;
		this.songClips = songClips;
		this.jda = jda;
		this.voiceChannelId = voiceChannel.getIdLong();
		this.guildId = guild.getIdLong();
		this.textChannelId = textChannel.getIdLong();
		currentIndex = 0;
		lives = 3;
	}

	@Override
	public void startUp()
	{
		startSong();
	}

	@Override
	protected void play(AudioTrack audioTrack)
	{
		Guild guild = jda.getGuildById(guildId);

		assert guild != null;

		musicManager.connectToVoiceChannel(
			guild.getAudioManager(),
			jda.getVoiceChannelById(voiceChannelId)
		);

		player.playTrack(audioTrack);
	}

	@Override
	public synchronized void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
	{
		if (endReason == AudioTrackEndReason.STOPPED)
		{
			return;
		}

		if (endReason != AudioTrackEndReason.FINISHED)
		{
			log.error("Song shut down for reason: " + endReason);
			shutDown();
			return;
		}

		int currentIndex = this.currentIndex;

		executorService.schedule(() -> {
			if (currentIndex == this.currentIndex)
			{
				songEnd();
			}
		}, 10, TimeUnit.SECONDS);
	}

	@Override
	public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event)
	{
		if (event.getMember().getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong())
		{
			return;
		}
		musicManager.getPlayer().stopTrack();
		shutDown();
	}

	@Override
	public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event)
	{
		if (event.getMember().getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong())
		{
			return;
		}
		musicManager.getPlayer().stopTrack();
		shutDown();
	}

	@Override
	public synchronized void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
	{
		if (event.getChannel().getIdLong() == textChannelId
			&& songAnswer.process(event.getMessage().getContentRaw(), event.getAuthor()))
		{
			event.getMessage().addReaction("\uD83D\uDE03").queue();

			long userId = event.getAuthor().getIdLong();

			scores.put(userId, scores.getOrDefault(userId, 0d) + .5);

			if (songAnswer.isAnswered())
			{
				player.stopTrack();
				songEnd();
			}
		}
	}

	private synchronized void startSong()
	{
		SongClip currentSong = songClips.get(currentIndex);
		songAnswer = new SongAnswer(currentSong);
		loadAndPlay(currentSong);
	}

	private synchronized void songEnd()
	{
		SongClip currentSong = songClips.get(currentIndex);
		if (!songAnswer.isAnswered())
		{
			lives--;
			sendMessage("Correct Answer: Group `" + currentSong.getGroup() + "` Song Name `" + currentSong.getSongName() + "`");
			if (lives == 0)
			{
				gameOver();
				return;
			}
			sendMessage("Current Lives: " + lives);
		}

		currentIndex++;

		if (currentIndex >= songClips.size())
		{
			sendMessage("No more songs, shit owner :(");
			gameOver();
			return;
		}

		startSong();
	}

	private void gameOver()
	{
		sendMessage("Game over. Scores:");
		Map<Long, Double> sortedScores = Util.sortByValue(scores);
		for (Long userId : sortedScores.keySet())
		{
			User user = jda.getUserById(userId);
			if (user != null)
			{
				sendMessage(user.getName() + ": " + sortedScores.get(userId));
			}
		}
		shutDown();
	}

	private void sendMessage(String message)
	{
		TextChannel textChannel = jda.getTextChannelById(textChannelId);

		if (textChannel == null)
		{
			log.error("Text channel is null somehow");
			shutDown();
			return;
		}

		textChannel.sendMessage(message).queue();
	}
}
