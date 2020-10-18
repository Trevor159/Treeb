package gg.trevor.treeb.bot.settings;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SettingsRepository extends MongoRepository<Settings, Long>
{
}
