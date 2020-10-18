package gg.trevor.treeb.bot.commands;

import com.jagrosh.jdautilities.command.Command;

public abstract class OwnerCommand extends Command
{
	public OwnerCommand()
	{
		this.category = new Category("Owner", event ->
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

			return false;
		});

		this.guildOnly = true;
	}
}