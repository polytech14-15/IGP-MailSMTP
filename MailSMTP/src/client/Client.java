package client;

import java.util.HashMap;
import java.util.Map;


public class Client {
    private final Map<String,String> listDomain;
    private Map<String,Integer> infoDestination;
    
    public Client(){
        this.listDomain = new HashMap<String,String>();
        this.listDomain.put("toto.fr", "127.0.0.1:2009");
        
        this.infoDestination = new HashMap<String,Integer>();
    }
    
    private void getInfoDestination(){
        System.out.println("hello");
    }
    
    public void start(){
        this.getInfoDestination();
        
    }
}
