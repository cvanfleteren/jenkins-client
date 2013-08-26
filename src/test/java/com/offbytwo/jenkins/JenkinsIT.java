package com.offbytwo.jenkins;


import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;

@Test(groups = "functional")
public class JenkinsIT {

    private Process process = null;
    private JenkinsServer server;

    @BeforeGroups(groups = "functional")
    public void setup() throws  Exception {

        ProcessBuilder pb = new ProcessBuilder();
        pb.directory(new File("./target/jenkins/"));
        pb.command("java" ,"-DJENKINS_HOME=./jenkins-home","-jar","jenkins.war");
        pb.redirectErrorStream(true);

        process = pb.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                process.destroy();
            }
        });

        server = new JenkinsServer(URI.create("http://localhost:8080"));


        while(true) {
            try {
                server = new JenkinsServer(URI.create("http://localhost:8080"));
                server.getJobs();
                break;
            } catch (IOException e) {

            }
        }
    }

    @AfterGroups(groups = "functional", alwaysRun = true)
    public void teardown() throws Exception  {
        process.destroy();
    }

    public void testEmpty() throws  Exception {
        Assert.assertTrue(server.getJobs().isEmpty());
    }

    @Test(dependsOnMethods = "testEmpty")
    public void testCreateJob() throws  Exception {
       String xml =  "<project>" +
            "<actions/>" +
            "<description/>" +
            "<keepDependencies>false</keepDependencies>" +
            "<properties/>" +
            "<scm class='hudson.scm.NullSCM'/>" +
            "<canRoam>true</canRoam>" +
            "<disabled>false</disabled>" +
            "<blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>" +
            "<blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>" +
            "<triggers/>" +
            "<concurrentBuild>false</concurrentBuild>" +
            "<builders/>" +
            "<publishers/>" +
            "<buildWrappers/>" +
        "</project>";
        server.createJob("test1",xml);

        Assert.assertEquals(server.getJobs().size(),1);
    }

}
