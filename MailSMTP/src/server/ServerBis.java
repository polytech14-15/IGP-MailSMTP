package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class ServerBis {
    
    private static final int PORT = 3333;
    public static final String DOMAIN = "toto.fr";
    public static final List<String> USERS = new ArrayList<String>(){
            {
                add("john");
                add("mike");
            }
    };

    public static void main(String args[]) throws IOException {
        ServerSocket s = new ServerSocket(PORT);
        System.out.println("Server running on port: "+PORT);
        while(true) new ServerThread(s.accept());
    }
}
