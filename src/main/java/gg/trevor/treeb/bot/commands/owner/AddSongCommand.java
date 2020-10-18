package gg.trevor.treeb.bot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import gg.trevor.treeb.Util;
import gg.trevor.treeb.bot.audio.songguess.Difficulty;
import gg.trevor.treeb.bot.audio.songguess.SongClip;
import gg.trevor.treeb.bot.audio.songguess.SongClipRepository;
import gg.trevor.treeb.bot.commands.OwnerCommand;
import gg.trevor.treeb.songdownloader.AudioDownloader;
import org.springframework.beans.factory.annotation.Autowired;

public class AddSongCommand extends OwnerCommand
{
	@Autowired
	private AudioDownloader audioDownloader;

	@Autowired
	private SongClipRepository songClipRepository;

	public AddSongCommand()
	{
		this.name = "addsong";
		this.help = "Adds a song to the song guesser";
		this.arguments = "<group> <song name> <easy | medium | hard> <start time (in seconds)> <stop time (in seconds)> <URL>";
	}

	@Override
	protected void execute(CommandEvent event)
	{
		if (event.getArgs().isEmpty())
		{
			event.replyError("Proper arguments: " + this.arguments);
			return;
		}

		String[] args = Util.parseArgs(event.getArgs());

		if (args.length != 6)
		{
			event.replyError("Invalid Argument count");
			return;
		}
		String group = args[0];
		String songName = args[1];

		Difficulty difficulty = Difficulty.get(args[2]);

		if (difficulty == null)
		{
			event.replyError("Invalid difficulty type");
			return;
		}
		Double startTime = Util.parseNumber(args[3]);
		Double stopTime = Util.parseNumber(args[4]);

		if (startTime == null || stopTime == null)
		{
			event.replyError("Cannot parse start or stop time.");
			return;
		}

		String url = args[5];

		SongClip songClip = new SongClip();
		songClip.setGroup(group);
		songClip.setSongName(songName);
		songClip.setDifficulty(difficulty);
		songClip.setStartTime(startTime);
		songClip.setStopTime(stopTime);
		songClip.setUrl(url);

		if (!audioDownloader.downloadSong(songClip))
		{
			event.replyError("Something went wrong when downloading the song. Try again or try a different url.");
			return;
		}

		songClipRepository.save(songClip);

		event.replySuccess("Added song with ID: " + songClip.getId());
	}
}
