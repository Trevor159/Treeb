package gg.trevor.treeb.songdownloader;

import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import gg.trevor.treeb.bot.audio.songguess.SongClip;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AudioDownloader
{
	@Value("${path.downloads}")
	private String DOWNLOAD_PATH;

	@Value("${path.ffmpeg}")
	private String FFMPEG_PATH;

	@Value("${path.ffprobe}")
	private String FFPROBE_PATH;

	@Value("${path.youtubedl}")
	private String YOUTUBE_DL_PATH;

	private FFmpeg ffmpeg;
	private FFprobe ffprobe;

	@PostConstruct
	public void init() throws IOException
	{
		YoutubeDL.setExecutablePath(YOUTUBE_DL_PATH);
		ffmpeg = new FFmpeg(FFMPEG_PATH);
		ffprobe = new FFprobe(FFPROBE_PATH);
	}

	public boolean downloadSong(SongClip songClip)
	{
		try
		{
			long startTime = ((Double) (songClip.getStartTime() * 1000)).longValue();
			long stopTime = ((Double) (songClip.getStopTime() * 1000)).longValue();

			String extension = ".opus";
			String temp_file = DOWNLOAD_PATH + "/temp" + extension;

			String outputFileName = songClip.getGroup() + "-" + songClip.getSongName() + "-" + startTime + "-" + stopTime;
			outputFileName = outputFileName.replaceAll("[^a-zA-Z0-9]", "");

			File file;

			for (int i = 0; true; i++)
			{
				String tempOutputName = outputFileName;

				if (i != 0)
				{
					tempOutputName += "_" + i;
				}

				tempOutputName += extension;

				file = new File(DOWNLOAD_PATH + "/" + tempOutputName);

				if (!file.exists())
				{
					outputFileName = tempOutputName;
					break;
				}
			}

			file = new File(temp_file);
			if (file.exists())
			{
				file.delete();
			}

			YoutubeDLRequest request = new YoutubeDLRequest(songClip.getUrl(), DOWNLOAD_PATH);
			request.setOption("format", "bestaudio");
			request.setOption("extract-audio");
			request.setOption("audio-format", "opus");
			request.setOption("output", "temp.%(ext)s");
			request.setOption("no-playlist");

			YoutubeDLResponse response = YoutubeDL.execute(request);

			FFmpegBuilder builder = new FFmpegBuilder()
				.setInput(temp_file)
				.setStartOffset(startTime, TimeUnit.MILLISECONDS)
				.overrideOutputFiles(true)
				.addOutput(DOWNLOAD_PATH + "/" + outputFileName)
					.setFormat("opus")
					.setDuration(stopTime - startTime, TimeUnit.MILLISECONDS)
					.done();

			FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
			executor.createJob(builder).run();

			if (file.exists())
			{
				file.delete();
			}

			String previousFileName = songClip.getFileName();

			if (previousFileName != null)
			{
				file = new File(DOWNLOAD_PATH + "/" + previousFileName);

				if (file.exists())
				{
					file.delete();
				}
			}

			songClip.setFileName(outputFileName);

			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
