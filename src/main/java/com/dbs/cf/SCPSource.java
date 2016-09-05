package com.dbs.cf;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.dbs.cf.CopyFileUtils.checkAck;

/**
 * Created by cq on 4/9/16.
 */
@EnableBinding(SCPSource.OutputSource.class)
@EnableConfigurationProperties(SCPSourceOptionsMetadata.class)
public class SCPSource {

    Logger logger = LoggerFactory.getLogger(SCPSource.class);

    @Autowired
    private SCPSourceOptionsMetadata options;

    private OutputSource outputChannel;

    @Autowired
    public void setChannel(OutputSource channel) {
        this.outputChannel = channel;
    }

    public String processData(){

        try {

            String localFileName = copyFileOver();
            streamFileContent(localFileName);
            return localFileName;

        } catch (IOException e) {
            logger.error("error processing file: ", e);

        }
        return null;

    }

    private String copyFileOver() throws IOException{

        JSch jsch=new JSch();
        FileOutputStream outputStream=null;
        String localFile = "/tmp/output.scp." + System.currentTimeMillis();

        try {

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");

            jsch.addIdentity(options.getPrivateKeyFile());
            Session session=jsch.getSession(options.getUsername(), options.getHostname(), options.getPort());
            session.setConfig(config);

            session.connect();
            // exec 'scp -f rfile' remotely
            String command="scp -f " + options.getFileName();
            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();
            channel.connect();



            String prefix=null;
            if(new File(localFile).isDirectory()){
                prefix= localFile + File.separator;
            }

            byte[] buf=new byte[1024];

            // send '\0'
            buf[0]=0;
            out.write(buf, 0, 1);
            out.flush();

            while(true){

                int c=checkAck(in);

                if(c!='C'){
                    break;
                }

                // read '0644 '
                in.read(buf, 0, 5);

                long filesize=0L;

                while(true){

                    if(in.read(buf, 0, 1)<0){
                        // error
                        break;
                    }

                    if(buf[0]==' ') break;

                    filesize = filesize * 10L + (long)(buf[0]-'0');
                }

                String file;

                for(int i=0;;i++){
                    in.read(buf, i, 1);
                    if(buf[i]==(byte)0x0a){
                        file=new String(buf, 0, i);
                        break;
                    }
                }

                logger.info("filesize="+filesize+", file="+file);

                // send '\0'
                buf[0]=0;
                out.write(buf, 0, 1);
                out.flush();

                // read a content of localFile
                outputStream=new FileOutputStream(prefix==null ? localFile : prefix+file);
                int tempFileSize;

                while(true){

                    if(buf.length < filesize) tempFileSize = buf.length;

                    else tempFileSize=(int)filesize;

                    tempFileSize=in.read(buf, 0, tempFileSize);

                    if(tempFileSize<0){
                        // error
                        break;
                    }
                    outputStream.write(buf, 0, tempFileSize);
                    filesize-=tempFileSize;
                    if(filesize==0L) break;
                }
                outputStream.close();
                outputStream=null;

                if(checkAck(in)!=0){
                    System.exit(0);
                }

                // send '\0'
                buf[0]=0;
                out.write(buf, 0, 1);
                out.flush();
            }

            session.disconnect();
            return localFile;

        } catch (JSchException e) {

            logger.error("error cp file",e);

            throw new IOException(e);

        } catch(IOException e){
            try {
                if(outputStream!=null) outputStream.close();
            } catch(Exception ee){
                logger.error("error closing file",ee);
            }
            throw e;
        }finally{
            try {
                if(outputStream!=null) outputStream.close();
            } catch(Exception ee){
                logger.error("error closing file",ee);
            }
        }


    }


    private void streamFileContent(String fileName) throws IOException{

        Files.
                lines(Paths.get(fileName)).
                forEach(line -> outputChannel.
                        output().
                        send(MessageBuilder.
                        withPayload(line).
                                build()));
    }


    public interface OutputSource {

        String OUTPUT = "output";

        @Output(OutputSource.OUTPUT)
        MessageChannel output();
    }

}
