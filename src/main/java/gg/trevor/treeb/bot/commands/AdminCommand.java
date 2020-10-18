package gg.trevor.treeb.bot.commands;

import com.jagrosh.jdautilities.command.Command;
import net.dv8tion.jda.api.Permission;

public abstract class AdminCommand extends Command
{
	public AdminCommand()
	{
		this.category = new Category("Admin", event ->
		{
			if(event.getAuthor().getIdLong() == event.getClient().getOwnerIdLong())
			{
				return true;
			}

			for (long id : event.getClient().getCoOwnerIdsLong())
			{
				if (event.getAuthor().getIdLong() == id)
				{
					return true;
				}
			}

			return event.getMember().hasPermission(Permission.MANAGE_SERVER);
		});

		this.guildOnly = true;
	}
}