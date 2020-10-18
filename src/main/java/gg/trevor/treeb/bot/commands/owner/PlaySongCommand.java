package gg.trevor.treeb.bot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import gg.trevor.treeb.Util;
import gg.trevor.treeb.bot.audio.AudioHandler;
import gg.trevor.treeb.bot.audio.GuildMusicManager;
import gg.trevor.treeb.bot.audio.individualplay.IndividualPlayEventAdaptor;
import gg.trevor.treeb.bot.audio.songguess.SongClip;
import gg.trevor.treeb.bot.audio.songguess.SongClipRepository;
import gg.trevor.treeb.bot.commands.OwnerCommand;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionFailedException;

public class PlaySongCommand extends OwnerCommand
{
	@Autowired
	private AudioHandler audioHandler;

	@Autowired
	private SongClipRepository songClipRepository;

	@Value("${path.downloads}")
	private String DOWNLOAD_PATH;

	public PlaySongCommand()
	{
		this.name = "playsong";
		this.help = "play song test";
		this.arguments = "<song id>";
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().isEmpty())
		{
			event.replyError("Needs an id as an argument");
			return;
		}

		//TODO: check if the bot is already playing and make more smart

		String[] args = Util.parseArgs(event.getArgs());
		String id = args[0];

		Optional<SongClip> songClip;

		try
		{
			songClip = songClipRepository.findById(id);
		}
		catch (ConversionFailedException e)
		{
			event.replyError("Invalid id");
			return;
		}


		if (songClip.isEmpty())
		{
			event.replyError("There is no song with that id");
			return;
		}

		Member member = event.getMember();
		if (member.getVoiceState() == null || member.getVoiceState().getChannel() == null)
		{
			event.replyError("You need to be in a voice channel to use this command");
			return;
		}

		GuildMusicManager musicManager = audioHandler.getOrCreateGuildAudioPlayer(event.getGuild());
		IndividualPlayEventAdaptor eventAdaptor = new IndividualPlayEventAdaptor(
			musicManager,
			songClip.get(),
			event.getGuild(),
			member.getVoiceState().getChannel()
		);

		if (!musicManager.startEventAdaptor(eventAdaptor))
		{
			event.replyError("The bot is already busy in this server");
		}
	}

}