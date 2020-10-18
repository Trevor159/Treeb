package gg.trevor.treeb.bot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import gg.trevor.treeb.bot.audio.songguess.SongClip;
import gg.trevor.treeb.bot.audio.songguess.SongClipRepository;
import gg.trevor.treeb.bot.commands.OwnerCommand;
import java.io.File;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class DeleteSongCommand extends OwnerCommand
{
	@Autowired
	private SongClipRepository songClipRepository;

	@Value("${path.downloads}")
	private String DOWNLOAD_PATH;

	public DeleteSongCommand()
	{
		this.name = "deletesong";
		this.help = "delete song test";
		this.arguments = "<song id>";
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().isEmpty())
		{
			event.replyError("Needs an id as an argument");
			return;
		}

		String[] args = event.getArgs().split(" ");
		String id = args[0];

		Optional<SongClip> songClip = songClipRepository.findById(id);

		if (songClip.isEmpty())
		{
			event.replyError("There is no song with that id");
			return;
		}

		String filePath = DOWNLOAD_PATH + "/" + songClip.get().getFileName();
		File file = new File(filePath);

		if (file.exists())
		{
			file.delete();
		}

		songClipRepository.delete(songClip.get());

		event.reactSuccess();
	}

}