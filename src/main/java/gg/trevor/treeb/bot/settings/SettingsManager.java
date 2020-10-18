package gg.trevor.treeb.bot.settings;

import com.jagrosh.jdautilities.command.GuildSettingsManager;
import java.util.HashMap;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

public class SettingsManager implements GuildSettingsManager
{
	@Autowired
	private SettingsRepository settingsRepository;

	private final HashMap<Long,Settings> settings;

	public SettingsManager()
	{
		this.settings = new HashMap<>();
	}

	@Nullable
	@Override
	public Settings getSettings(Guild guild)
	{
		return getSettings(guild.getIdLong());
	}

	public Settings getSettings(long guildId)
	{
		return settings.computeIfAbsent(guildId, id -> {
			Optional<Settings> setting = settingsRepository.findById(guildId);
			return setting.orElseGet(() -> createDefaultSettings(id));
		});
	}

	private Settings createDefaultSettings(Long id)
	{
		return new Settings(this, id, null);
	}

	public void save(Settings settings)
	{
		settingsRepository.save(settings);
	}
}
