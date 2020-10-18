package gg.trevor.treeb.bot.settings;

import com.jagrosh.jdautilities.command.GuildSettingsProvider;
import java.util.Collection;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
public class Settings implements GuildSettingsProvider
{
	@Transient
	private final SettingsManager settingsManager;

	@Id
	private Long id;
	private String prefix;

	public void save()
	{
		settingsManager.save(this);
	}

	@Override
	public Collection<String> getPrefixes()
	{
		return prefix == null ? Collections.EMPTY_SET : Collections.singleton(prefix);
	}
}
