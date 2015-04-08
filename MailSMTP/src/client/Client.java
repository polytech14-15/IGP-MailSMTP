package client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client {
    private final Map<String,String> listDomain;
    private Map<String,ArrayList<String>> infoDestination;
    
    private Socket socket_server;
    
    private String state;
    
    public Client(){
        //Initialise la liste des domaines connus par le client
        this.listDomain = new HashMap<String,String>();
        this.listDomain.put("toto.fr", "134.214.117.127:2009");
        
        this.infoDestination = new HashMap<String,ArrayList<String>>();
    }
    
    private void getInfoDestination(){
        System.out.println("hello");
    }
    
    public void start(){
        this.getInfoDestination();
        
//        todelete - pour les test
//        ArrayList<String> test = new ArrayList<>();
//        test.add("ser");
//        this.infoDestination.put("toto.fr", test);
        
        // Pour chaque domaine trouvé
        for (Map.Entry<String, ArrayList<String>> entry : infoDestination.entrySet())
        {
            //System.out.println(entry.getKey() + "/" + entry.getValue());
            
            initSocket(entry.getKey());
        }
  
        
    }
    
    /**
     * Recupere et creer la connexion avec le serveur a contacter
     * @param domain - Nom du domaine a contacter
     */
    private void initSocket(String domain){
        String infos = this.listDomain.get(domain);
        
        String ip = infos.split(":")[0];
        int port = Integer.parseInt(infos.split(":")[1]);
        
        try {
            this.socket_server = new Socket(ip, port);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.state = "INIT";
    }
    
}
