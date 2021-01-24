# JSSH
Wrapping a few methods around JSch to make connecting easier etc. 

#Features
Connect using username, password/key and format output.

Password Authentication:
`````````````
  List<String> commands = Arrays.asList("whoami","df -h");
`````````````
`````````````
  SSHShell shell = SSHShell.builder()
                .hostName(hostName)
                .username(username)
                .password(password)
                .port(port)
                .build();
`````````````
  Key Authentication: 
`````````````
  SSHShell shell = SSHShell.builder()
                .keyAuth(true)
                .key(keyLocation)
                .username(username)
                .hostName(hostName)
                .port(port)
                .build();   
`````````````
`````````````
  List<String> output = shell.getOutput(commands);
`````````````
Output Example: 
`````````````
  Last login: Sat Jan 23 23:58:33 2021 from 192.168.0.88

  whoami
  sudo su
  df -h
  exit
  exit
  person@hostname:~$ whoami
  person
  person@hostname:~$ sudo su
  root@hostname:/home/person# df -h
  Filesystem      Size  Used Avail Use% Mounted on
  udev            3.8G     0  3.8G   0% /dev
  tmpfs           768M  1.6M  767M   1% /run
  /dev/sda1       229G   70G  148G  32% /
  ........
  root@hostname:/home/person# exit
  exit
  person@hostname:~$ exit
  logout
`````````````
The above example will give the entire shell session output as a list. If you want to just get the output of the commands (Output of commands "exit, logout and sudo are always ignored") set the format output to 'true'.
`````````````
  SSHShell shell = SSHShell.builder()
        .hostName(hostName)
        .username(username)
        .password(password)
        .formatOutput(true) <------- this will return a formatted output.
        .port(port)
        .build();
`````````````
`````````````
  List<String> output = shell.getOutput(commands);
`````````````
Output:
`````````````
  person <--------Output of "whoami"

  Filesystem      Size  Used Avail Use% Mounted on <-----------Output of "df -h"
  udev            3.8G     0  3.8G   0% /dev
  tmpfs           768M  1.6M  767M   1% /run
  /dev/sda1       229G   70G  148G  32% /
  .........
`````````````

  
