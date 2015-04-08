package client;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Client {
    private final Map<String,String> listDomain;
    private Map<String,ArrayList<String>> infoDestination;
    
    public Client(){
        this.listDomain = new HashMap<String,String>();
        this.listDomain.put("toto.fr", "127.0.0.1:2009");
        
        this.infoDestination = new HashMap<String,ArrayList<String>>();
    }
    
    private void getInfoDestination(){
        System.out.println("hello");
    }
    
    public void start(){
        this.getInfoDestination();
        
        
        
    }
}
