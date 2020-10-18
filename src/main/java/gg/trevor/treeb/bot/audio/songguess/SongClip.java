package gg.trevor.treeb.bot.audio.songguess;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Data
public class SongClip
{
	@MongoId(value = FieldType.OBJECT_ID, targetType = FieldType.OBJECT_ID)
	private String id;

	private String group;
	private String songName;
	private String url;
	private Difficulty difficulty;
	private Double startTime;
	private Double stopTime;

	private String fileName;
}
