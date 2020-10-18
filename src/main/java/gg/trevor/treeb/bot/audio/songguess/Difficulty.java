package gg.trevor.treeb.bot.audio.songguess;

public enum Difficulty
{
	EASY,
	MEDIUM,
	HARD,
	ALL;

	public static Difficulty get(String s)
	{
		for (Difficulty difficulty : Difficulty.values())
		{
			if (s.equalsIgnoreCase(difficulty.name()))
			{
				return difficulty;
			}
		}

		return null;
	}
}
