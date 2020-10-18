package gg.trevor.treeb.bot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import gg.trevor.treeb.Util;
import gg.trevor.treeb.bot.audio.songguess.Difficulty;
import gg.trevor.treeb.bot.audio.songguess.SongClip;
import gg.trevor.treeb.bot.audio.songguess.SongClipRepository;
import gg.trevor.treeb.bot.commands.OwnerCommand;
import gg.trevor.treeb.songdownloader.AudioDownloader;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionFailedException;

public class EditSongCommand extends OwnerCommand
{
	@Autowired
	private AudioDownloader audioDownloader;

	@Autowired
	private SongClipRepository songClipRepository;

	public EditSongCommand()
	{
		this.name = "editsong";
		this.help = "Edits a song in the song guesser";
		this.arguments = "<id> <group | name | difficulty | time>";
	}

	@Override
	protected void execute(CommandEvent event)
	{
		String[] args = Util.parseArgs(event.getArgs());

		if (event.getArgs().isEmpty() || args.length < 2)
		{
			event.replyError("Proper arguments: " + this.arguments);
			return;
		}

		String id = args[0];

		Optional<SongClip> optionalSongClip;

		try
		{
			optionalSongClip = songClipRepository.findById(id);
		}
		catch (ConversionFailedException e)
		{
			event.replyError("Invalid id");
			return;
		}

		if (optionalSongClip.isEmpty())
		{
			event.replyError("There is no song with that id");
			return;
		}

		SongClip songClip = optionalSongClip.get();

		String mode = args[1];

		if (mode.equalsIgnoreCase("group"))
		{
			if (args.length == 2)
			{
				event.replyError("Proper arguments: <id> group <group name>");
				return;
			}

			String groupName = args[2];
			songClip.setGroup(groupName);
		}
		else if (mode.equalsIgnoreCase("name"))
		{
			if (args.length == 2)
			{
				event.replyError("Proper arguments: <id> name <song name>");
				return;
			}

			String songName = args[2];
			songClip.setSongName(songName);
		}
		else if (mode.equalsIgnoreCase("difficulty"))
		{
			if (args.length == 2)
			{
				event.replyError("Proper arguments: <id> difficulty <easy | medium | hard>");
				return;
			}

			Difficulty difficulty = Difficulty.get(args[2]);

			if (difficulty == null)
			{
				event.replyError("Invalid difficulty type");
				return;
			}

			songClip.setDifficulty(difficulty);
		}
		else if (mode.equalsIgnoreCase("time"))
		{
			if (args.length == 2 || args.length == 3)
			{
				event.replyError("Proper arguments: <id> time <start time> <stop time>");
				return;
			}

			Double startTime = Util.parseNumber(args[2]);
			Double stopTime = Util.parseNumber(args[3]);

			if (startTime == null || stopTime == null)
			{
				event.replyError("Cannot parse start or stop time.");
				return;
			}

			songClip.setStartTime(startTime);
			songClip.setStopTime(stopTime);

			if (!audioDownloader.downloadSong(songClip))
			{
				event.replyError("Something went wrong when downloading the song. Try again or try a different url.");
				return;
			}
		}
		else
		{
			event.replyError("Proper arguments: " + this.arguments);
			return;
		}


		songClipRepository.save(songClip);
		event.replySuccess("Edited song with ID: " + songClip.getId());
	}
}
