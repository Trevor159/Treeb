package gg.trevor.treeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HelloDiscordApplication
{

	public static void main(String[] args)
	{
		SpringApplication.run(HelloDiscordApplication.class, args);
	}

}
