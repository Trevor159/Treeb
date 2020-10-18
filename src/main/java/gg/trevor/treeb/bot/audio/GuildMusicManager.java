package gg.trevor.treeb.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lombok.Getter;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildMusicManager {
	// 5 minutes in ms
	private static final long INACTIVITY_TIMEOUT = 2 * 60 * 1000;

	@Getter
	private final AudioPlayerManager playerManager;

	@Getter
	private final AudioPlayer player;

//	@Getter
//	private final AudioPlayerSendHandler audioPlayerSendHandler;

	@Getter
	private CombinedEventAdapter eventAdapter;

	private long lastActive;

	public GuildMusicManager(AudioPlayerManager playerManager) {
		this.playerManager = playerManager;
		player = playerManager.createPlayer();
//		audioPlayerSendHandler = new AudioPlayerSendHandler(player);
	}

	public boolean startEventAdaptor(CombinedEventAdapter combinedEventAdapter)
	{
		if (eventAdapter != null)
		{
			return false;
		}

		eventAdapter = combinedEventAdapter;
		eventAdapter.startUp();
		player.addListener(eventAdapter);
		return true;
	}

	public void stopEventAdaptor()
	{
		player.stopTrack();
		player.removeListener(eventAdapter);
		eventAdapter = null;
		lastActive = System.currentTimeMillis();
	}


	public void connectToVoiceChannel(AudioManager audioManager, VoiceChannel voiceChannel) {
		if (!audioManager.isConnected()
			|| (audioManager.getConnectedChannel() != null
			&& !audioManager.getConnectedChannel().equals(voiceChannel))
		)
		{
			audioManager.openAudioConnection(voiceChannel);
		}
	}

	public void disconnectFromVoiceChannel(AudioManager audioManager)
	{
		if (audioManager.isConnected())
		{
			audioManager.closeAudioConnection();
		}
	}

	public boolean isInactive()
	{
		long timeDifference = System.currentTimeMillis() - lastActive;
		if (eventAdapter == null && timeDifference > INACTIVITY_TIMEOUT)
		{
			return true;
		}

		return false;
	}
}
