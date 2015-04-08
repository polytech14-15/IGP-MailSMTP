package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {
    public static final int MAX_CON = 10;
    public static final int PORT_S = 2009;
    
    ServerSocket server;
   
    
    public Server(){
        
    }
    
    public static void main(String[] args) {
        Server s = new Server();
        try {
            s.startServeur();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void startServeur() throws IOException{
        
        server = new ServerSocket(PORT_S);
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CON);
        
        
        while(!server.isClosed()){
            Socket client = server.accept();            
            ServerCallable clientCallable = new ServerCallable(client);
            FutureTask<Socket> client2 = new FutureTask<Socket>(clientCallable);
            executor.execute(client2);
        }
        executor.shutdown(); 
   
    }
    
    
}
