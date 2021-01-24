package domain;

import com.jssh.domain.SSHShell;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class SSHShellTest {

    private List<String> commands;
    private String hostName;
    private String password;
    private final int port = 22;
    private String keyLocation;
    private String username;

    @Before
    public void setup(){
        commands = Arrays.asList("whoami", "sudo su", "df -h", "exit");
        hostName = "";
        username = "";
        password = "";
        keyLocation = "";
    }

    @Test
    public void testSSHShell(){
        SSHShell shell = SSHShell.builder()
                .hostName(hostName)
                .username(username)
                .password(password)
                .port(port)
                .build();

        List<String> output = shell.getOutput(commands);
        output.forEach(System.out::println);
    }

    @Test
    public void testSSHShellFormatOutput(){
        SSHShell shell = SSHShell.builder()
                .hostName(hostName)
                .username(username)
                .password(password)
                .formatOutput(true)
                .port(port)
                .build();
        List<String> output = shell.getOutput(commands);
        output.forEach(System.out::println);
    }

    @Test(expected = IllegalStateException.class)
    public void testPasswordNull(){
        SSHShell shell = SSHShell.builder()
                .hostName(hostName)
                .username(username)
                .port(port)
                .build();
        shell.getOutput(commands);
    }

    @Test(expected = IllegalStateException.class)
    public void testUsernameNull(){
        SSHShell shell = SSHShell.builder()
                .hostName(hostName)
                .password(password)
                .port(port)
                .build();
        shell.getOutput(commands);
    }

    @Test(expected = IllegalStateException.class)
    public void testHostNameNull(){
        SSHShell shell = SSHShell.builder()
                .password(password)
                .username(username)
                .port(port)
                .build();
        shell.getOutput(commands);
    }

    @Test(expected = IllegalStateException.class)
    public void testAllNull(){
        SSHShell shell = SSHShell.builder()
                .port(port)
                .build();
        shell.getOutput(commands);
    }

    @Test(expected = IllegalStateException.class)
    public void testKeyNull(){
        SSHShell shell = SSHShell.builder()
                .keyAuth(true)
                .username(username)
                .hostName(hostName)
                .port(port)
                .build();
        shell.getOutput(commands);
    }

    @Test
    public void testKeyBasedAuthentication(){
        SSHShell shell = SSHShell.builder()
                .keyAuth(true)
                .key(keyLocation)
                .username(username)
                .hostName(hostName)
                .port(port)
                .build();
        List<String> output = shell.getOutput(commands);
        assertNotNull(output);
        output.forEach(System.out::println);
    }
}
