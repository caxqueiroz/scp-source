package com.dbs.cf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ScpSourceApplicationTests.SCPProcessorTest.class)
@IntegrationTest({"server.port=-1"})
@DirtiesContext
public class ScpSourceApplicationTests {

    @Autowired
    SCPSource scpSource;

    @Autowired
    private BinderFactory<MessageChannel> binderFactory;

    @Autowired
    private MessageCollector messageCollector;

    @Autowired
    @Bindings(SCPProcessorTest.class)
    private Processor scpProcessorTest;

	@Test
	public void copyFile() throws Exception{

	    String localFileName = scpSource.processData();
        assertThat(Files.exists(Paths.get(localFileName)),is(true));
    }

    @Test
    public void testMessageOutput() {
        Message<String> message = new GenericMessage<>("hello");
        scpProcessorTest.input().send(message);


    }


    @SpringBootApplication
    @EnableBinding(Processor.class)
    public static class SCPProcessorTest {

        @Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
        private String processData(String data){
            return data + " : " + System.currentTimeMillis();
        }

    }

}
