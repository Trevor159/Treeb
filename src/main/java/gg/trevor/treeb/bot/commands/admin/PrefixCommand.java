package gg.trevor.treeb.bot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import gg.trevor.treeb.bot.commands.AdminCommand;
import gg.trevor.treeb.bot.settings.Settings;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class PrefixCommand extends AdminCommand
{
	public PrefixCommand()
	{
		this.name = "prefix";
		this.help = "sets a server-specific prefix";
		this.arguments = "<prefix|NONE>";
	}

	@Override
	protected void execute(CommandEvent event)
	{
		if(event.getArgs().isEmpty())
		{
			event.replyError("Please include a prefix or NONE");
			return;
		}

		Settings s = event.getClient().getSettingsFor(event.getGuild());
		if(event.getArgs().equalsIgnoreCase("none"))
		{
			s.setPrefix(null);
			s.save();
			event.replySuccess("Prefix cleared.");
		}
		else
		{
			s.setPrefix(event.getArgs());
			s.save();
			event.replySuccess("Custom prefix set to `" + event.getArgs() + "` on *" + event.getGuild().getName() + "*");
		}
	}
}
