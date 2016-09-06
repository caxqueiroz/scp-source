package com.dbs.cf;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by cq on 4/9/16.
 */
@ConfigurationProperties(prefix = "scp")
public class SCPSourceOptionsMetadata {

    /** hostname of remote server*/
    private String hostname;

    /** remote port */
    private int port = 22;

    /** username for login on remote server*/
    private String username;

    /** password to login remote server */
    private String password;

    /** remote file to be copied over */
    private String fileName;

    /** cron expression */
    private String cronExpression = "";


    private String privateKeyFile;

    public String getPrivateKeyFile() {
        return privateKeyFile;
    }

    public void setPrivateKeyFile(String privateKeyFile) {
        this.privateKeyFile = privateKeyFile;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
}
