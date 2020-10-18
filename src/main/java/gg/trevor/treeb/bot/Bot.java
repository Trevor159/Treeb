package gg.trevor.treeb.bot;

import com.google.common.reflect.ClassPath;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import gg.trevor.treeb.Util;
import gg.trevor.treeb.bot.settings.SettingsManager;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class Bot
{
	private static final String BOT_PACKAGE = "gg.trevor.treeb.bot";

	@Autowired
	private ApplicationContext applicationContext;

	@Value("${bot.token}")
	private String BOT_TOKEN;

	@Value("${bot.owner}")
	private String BOT_OWNER;

	@Value("#{'${bot.coowners}'.split(',')}")
	private String[] BOT_COOWNERS;

	@Value("${path.downloads}")
	private String DOWNLOAD_PATH;

	@Getter
	private JDA jda;

	@PostConstruct
	private void init() throws Exception
	{
		Util.DOWNLOAD_PATH = this.DOWNLOAD_PATH;

		List<Command> commands = new ArrayList<>();
		List<Object> eventClasses = new ArrayList<>();

		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		beanFactory.registerSingleton(ScheduledExecutorService.class.getName(), executorService);

		EventWaiter eventWaiter = new EventWaiter(executorService, true);
		beanFactory.registerSingleton(EventWaiter.class.getName(), eventWaiter);

//		ConstructorArgumentValues cargs = new ConstructorArgumentValues();
//		cargs.addIndexedArgumentValue(0, Executors.newSingleThreadScheduledExecutor());
//		cargs.addIndexedArgumentValue(1, true);
//		EventWaiter eventWaiter = (EventWaiter) instantiateClass(EventWaiter.class, cargs);
		eventClasses.add(eventWaiter);

		scanAndLoadClasses(commands, eventClasses);

		SettingsManager settings = (SettingsManager) instantiateClass(SettingsManager.class);

		CommandClient client = new CommandClientBuilder()
			.useDefaultGame()
			.setOwnerId(BOT_OWNER)
			.setCoOwnerIds(BOT_COOWNERS)
			.setEmojis("\uD83D\uDE03", "\uD83D\uDE2E", "\uD83D\uDE26")
			.setPrefix("..")
			.addCommands(commands.toArray(new Command[commands.size()]))
			.setGuildSettingsManager(settings)
			.build();

		eventClasses.add(client);

		jda = JDABuilder.createDefault(BOT_TOKEN)
			.setStatus(OnlineStatus.DO_NOT_DISTURB)
			.setActivity(Activity.playing("loading..."))
//			.setEventManager(new AnnotatedEventManager())
			.addEventListeners(eventClasses.toArray())
			.build();
	}

	private void scanAndLoadClasses(List<Command> commands, List<Object> eventClasses) throws IOException
	{
		ClassPath classPath = ClassPath.from(getClass().getClassLoader());

		List<Class<?>> clazzes = classPath.getTopLevelClassesRecursive(BOT_PACKAGE).stream()
			.map(ClassPath.ClassInfo::load)
			.collect(Collectors.toList());

//		clazzes.add(0, EventWaiter.class);

		for (Class<?> clazz : clazzes)
		{
			if (Modifier.isAbstract(clazz.getModifiers()))
			{
				continue;
			}

			if (Command.class.isAssignableFrom(clazz))
			{
				commands.add((Command) instantiateClass(clazz));
			}

			boolean hasEventAnnotation = false;

			for (Method method : clazz.getDeclaredMethods())
			{
				final SubscribeEvent sub = method.getAnnotation(SubscribeEvent.class);

				if (sub == null)
				{
					continue;
				}

				hasEventAnnotation = true;
				break;
			}

			if (hasEventAnnotation || EventListener.class.isAssignableFrom(clazz))
			{
				eventClasses.add(instantiateClass(clazz));
			}
		}
	}

	private Object instantiateClass(Class clazz)
	{
		return instantiateClass(clazz, null);
	}

	private Object instantiateClass(Class clazz, ConstructorArgumentValues cargs)
	{
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
		GenericBeanDefinition beanDef = new GenericBeanDefinition();
		beanDef.setBeanClass(clazz);
		beanDef.setAutowireCandidate(true);
		beanDef.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
		beanDef.setConstructorArgumentValues(cargs);
		String beanName = clazz.getName();
		beanFactory.registerBeanDefinition(beanName, beanDef);
		return applicationContext.getBean(clazz.getName());
	}
}
