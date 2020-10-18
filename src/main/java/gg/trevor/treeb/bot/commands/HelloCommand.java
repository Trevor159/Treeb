package gg.trevor.treeb.bot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class HelloCommand extends Command
{
	@Autowired
	private EventWaiter waiter;

	public HelloCommand()
	{
		this.name = "hello";
		this.aliases = new String[]{"hi"};
		this.help = "says hello and waits for a response";
	}

	@Override
	protected void execute(CommandEvent event)
	{
		// ask what the user's name is
		event.reply("Hello. What is your name?");

		// wait for a response
		waiter.waitForEvent(MessageReceivedEvent.class,
			// make sure it's by the same user, and in the same channel, and for safety, a different message
			e -> e.getAuthor().equals(event.getAuthor())
				&& e.getChannel().equals(event.getChannel())
				&& !e.getMessage().equals(event.getMessage()),
			// respond, inserting the name they listed into the response
			e -> event.reply("Hello, `"+e.getMessage().getContentRaw()+"`! I'm `"+e.getJDA().getSelfUser().getName()+"`!"),
			// if the user takes more than a minute, time out
			1, TimeUnit.MINUTES, () -> event.reply("Sorry, you took too long."));
	}

}