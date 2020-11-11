package gg.trevor.treeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TreebApplication
{

	public static void main(String[] args)
	{
		SpringApplication.run(TreebApplication.class, args);
	}

}
