package gg.trevor.treeb.bot.audio.songguess;

import gg.trevor.treeb.Util;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;

public class SongAnswer
{
	private final String group;
	private final String songName;

	@Getter
	private Long groupAnswerUser;

	@Getter
	private Long songNameAnswerUser;

	public SongAnswer(SongClip songClip)
	{
		this.group = songClip.getGroup();
		this.songName = songClip.getSongName();
	}

	public synchronized boolean process(String message, User user)
	{
		if (groupAnswerUser == null && Util.compareStrings(group, message))
		{
			groupAnswerUser = user.getIdLong();
			return true;
		}
		else if (songNameAnswerUser == null && Util.compareStrings(songName, message))
		{
			songNameAnswerUser = user.getIdLong();
			return true;
		}

		return false;
	}

	public synchronized boolean isAnswered()
	{
		if (groupAnswerUser != null && songNameAnswerUser != null)
		{
			return true;
		}

		return false;
	}

}
