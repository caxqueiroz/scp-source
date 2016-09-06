package com.dbs.cf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ScpSourceApp.class)
@IntegrationTest({"server.port=-1"})
public class ScpSourceApplicationTests {

    @Autowired
    private SCPSource scpSource;


    @Test
    public void testContextLoads() {

        assertNotNull(scpSource);
    }




}
