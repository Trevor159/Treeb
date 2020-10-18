package gg.trevor.treeb.bot.audio.songguess;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import gg.trevor.treeb.Util;
import gg.trevor.treeb.bot.audio.AudioHandler;
import gg.trevor.treeb.bot.audio.GuildMusicManager;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;

public class SongGuessCommand extends Command
{
	@Autowired
	private AudioHandler audioHandler;

	@Autowired
	private ScheduledExecutorService executorService;

	@Autowired
	private SongClipRepository songClipRepository;

	public SongGuessCommand()
	{
		this.name = "songguess";
		this.help = "command to start song guess games.";
		this.arguments = "<start>";
	}

	@Override
	protected void execute(CommandEvent event)
	{
		if (event.getArgs().isEmpty())
		{
			event.replyError("This command needs arguments: " + this.getArguments());
			return;
		}

		String[] args = Util.parseArgs(event.getArgs());

		if (args[0].equalsIgnoreCase("start"))
		{
			GuildMusicManager musicManager = audioHandler.getOrCreateGuildAudioPlayer(event.getGuild());

			Member member = event.getMember();
			if (member.getVoiceState() == null || member.getVoiceState().getChannel() == null)
			{
				event.replyError("You need to be in a voice channel to use this command");
				return;
			}

			List<SongClip> songClips = songClipRepository.findAll();
			Collections.shuffle(songClips);

			SongGuessEventAdaptor eventAdaptor = new SongGuessEventAdaptor(
				musicManager,
				executorService,
				songClips,
				event.getJDA(),
				member.getVoiceState().getChannel(),
				event.getGuild(),
				event.getTextChannel()
			);

			if (!musicManager.startEventAdaptor(eventAdaptor))
			{
				event.replyError("The bot is already busy in this server");
			}
		}
	}
}
