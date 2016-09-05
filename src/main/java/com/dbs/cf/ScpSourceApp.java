package com.dbs.cf;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScpSourceApp implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(ScpSourceApp.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {

	}
}
