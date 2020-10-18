package gg.trevor.treeb.bot.commands.owner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jagrosh.jdautilities.command.CommandEvent;
import gg.trevor.treeb.bot.audio.songguess.SongClip;
import gg.trevor.treeb.bot.audio.songguess.SongClipRepository;
import gg.trevor.treeb.bot.commands.OwnerCommand;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionFailedException;

public class SongListCommand extends OwnerCommand
{
	@Value("${path.downloads}")
	private String DOWNLOAD_PATH;

	@Autowired
	private SongClipRepository songClipRepository;

	private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private boolean commandFree = true;

	public SongListCommand()
	{
		this.name = "songlist";
		this.help = "Outputs a file with a list of all of the songs";
		this.arguments = "<optional id>";
	}

	@Override
	protected synchronized void execute(CommandEvent event)
	{
		if (!event.getArgs().isEmpty())
		{
			String id = event.getArgs();
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

			event.reply(gson.toJson(songClip.get()));
			return;
		}

		if (!commandFree)
		{
			event.replyError("Command in use try again in a short while");
			return;
		}

		commandFree = false;

		try
		{
			List<SongClip> songClips = songClipRepository.findAll();
			String filePath = DOWNLOAD_PATH + "/songlist.json";
			File file = new File(filePath);
			if (file.exists())
			{
				file.delete();
			}

			FileWriter fileWriter = new FileWriter(filePath);
			gson.toJson(songClips, fileWriter);
			fileWriter.close();

			Message message = new MessageBuilder().append("Song List").build();
			event.getTextChannel().sendMessage(message).addFile(file).queue(msg -> {
				if (file.exists())
				{
					file.delete();
				}
				commandFree = true;
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
			commandFree = true;
		}
	}
}
