package com.dbs.cf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by cq on 4/9/16.
 */
@EnableBinding(SCPSource.OutputSource.class)
public class SCPSource implements JobExecutionListener {

    Logger logger = LoggerFactory.getLogger(SCPSource.class);

    private OutputSource outputChannel;

    @Autowired
    public void setChannel(OutputSource channel) {
        this.outputChannel = channel;
    }


    private void streamFileContent(String fileName) throws IOException{

        logger.info("Streaming file data: " + fileName);

        Files.
                lines(Paths.get(fileName)).
                forEach(line -> outputChannel.
                        output().
                        send(MessageBuilder.
                        withPayload(line).
                                build()));
        logger.info("finished streaming file data: " + fileName);
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {}

    @Override
    public void afterJob(JobExecution jobExecution) {

        try {

            String fileName = (String) jobExecution.
                    getExecutionContext().
                    get("filename");

            streamFileContent(fileName);

        } catch (IOException e) {
            logger.error("error processing file: ", e);
        }

    }


    public interface OutputSource {

        String OUTPUT = "output";

        @Output(OutputSource.OUTPUT)
        MessageChannel output();
    }

}