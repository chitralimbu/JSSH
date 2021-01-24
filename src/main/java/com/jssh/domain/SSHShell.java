package com.jssh.domain;

import com.jcraft.jsch.*;
import com.jssh.domain.constants.SSHConstants;
import lombok.Builder;
import lombok.Data;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Wrapping a few methods around JSCH to make connecting to remote host easier.
 * Connect with Password or Key.
 */

@Data
@Builder
public class SSHShell implements AutoCloseable{

    private String username;
    private String password;
    private String hostName;
    private OutputStream outputStream;
    private Channel channel;
    private Session session;
    private int port;
    private int timeout;
    private boolean formatOutput;
    private boolean keyAuth;
    private String key;



    /**
     *Default create session used to connect to remote server. Custom session can be provided by providing session object in constructor or builder.
     * @return Session to be used to connect to remote server.
     */
    private Session usePassword(){
        if(username == null || hostName == null || password == null) throw new IllegalStateException("Username, HostName or Password cannot be null");
        JSch jsch = new JSch();
        try{
            session = jsch.getSession(username, hostName, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "password");
        }catch (JSchException e){
            e.printStackTrace();
        }
        return session;
    }

    /*TODO*/
    /**
     * To login using a private key. Will attempt to connect to remote server using private key.
     * Make sure the public key is already added to the remote host.
     * Key location must be provided.
     * Password not required if using useKey.
     * @return Session to be used to connect to remote server.
     */
    private Session useKey(){
        if(username == null || hostName == null || key == null ) throw new IllegalStateException("Username, Key or HostName cannot be null");
        try {
            JSch jSch = new JSch();
            jSch.addIdentity(key);
            session = jSch.getSession(username, hostName, port);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return session;
    }

    /**
     *Will issue a list of commands to remote shell session, if a sudo command is provided username and password must be provided after the sudo command. If sudo command is provided a manual 'exit' command must be provided or the session will not end.
     * If formatOutput is set to 'true' then only the output will be returned, otherwise the entire shell session output will be provided.
     * @param commands - List of commands to execute
     * @return output from servers
     */
    public List<String> getOutput(List<String> commands){
        try{
            /*If no session is provided then use password by default*/
            this.session = keyAuth ? useKey() : usePassword();
            this.session.setTimeout(timeout);
            this.session.connect();
            this.channel = session.openChannel("shell");
            ((ChannelShell)channel).setPtyType("dumb");
            InputStream commandOutput = channel.getInputStream();
            this.outputStream = channel.getOutputStream();
            this.channel.connect();

            if(formatOutput) commands = formatCommands(commands);

            commands.forEach(this::flushCommand);
            flushCommand(SSHConstants.SSH_EXIT);
            InputStreamReader inputReader = new InputStreamReader(commandOutput);
            BufferedReader reader = new BufferedReader(inputReader);
            List<String> output = reader.lines().collect(Collectors.toList());
            inputReader.close();
            reader.close();
            return formatOutput ? formatOutput(output) : output;
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void flushCommand(String command){
        try{
            this.outputStream.write((command+"\r").getBytes(StandardCharsets.UTF_8));
            this.outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *Start basic shell session.
     *If no session is provided then the default usePassword will be used to create the session.
     */
    public void startTerminal(){
        try{
            this.session = keyAuth ? useKey() : usePassword();
            this.session.connect(timeout);
            this.channel = session.openChannel("shell");
            ((ChannelShell)channel).setPtyType("dumb");
            this.channel.setInputStream(System.in);
            this.channel.setOutputStream(System.out);
            this.channel.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    private List<String> formatCommands(List<String> commands){
        return commands.stream()
                .map(command -> {
                    if(!command.equalsIgnoreCase(SSHConstants.SSH_EXIT) && !command.contains("sudo")){
                        command = String.format("echo %s;%s;echo %s", SSHConstants.SSH_START, command, SSHConstants.SSH_END);
                    }
                    return command;
                }).collect(Collectors.toList());
    }

    private List<String> formatOutput(List<String> output){
        boolean start = false;
        List<String> formattedOutput = new ArrayList<>();
        StringBuilder outputBuilder = new StringBuilder();
        for(String out : output){
            if(out.equals(SSHConstants.SSH_START)){
                start = true;
                continue;
            }

            if(out.equals(SSHConstants.SSH_END)){
                start = false;
                formattedOutput.add(outputBuilder.toString());
                outputBuilder = new StringBuilder();
                continue;
            }

            if(start){
                outputBuilder.append(out).append("\n");
            }
        }
        return formattedOutput;
    }

    @Override
    public void close(){
            this.channel.disconnect();
            this.session.disconnect();
    }
}
