package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    
    //Liste des domaines connus par le serveur
    private final Map<String, String> listDomain;
    
    private String emetteur;
    private String destinataires;
    private String subject;
    private String textMail;
    private Map<String, ArrayList<String>> infoDestination;
    private int nbDest;
    
    private Socket socket_server;
    
    private String state;

    public Client(String emetteur, String destinataires, String subject, String textMail) {
        this.listDomain = new HashMap<>();
        this.listDomain.put("toto.fr", "127.0.0.1:3333");
        this.emetteur = emetteur;
        this.destinataires = destinataires;
        this.subject = subject;
        this.textMail = textMail;
        this.infoDestination = new HashMap<>();
    }
    
    /**
     * Prepare la map infoDestination
     */
    private void getInfoDestination() {
        String[] tempAllDest = destinataires.split(";");
        for (int i = 0; i < tempAllDest.length; i++) {
            String[] tempDestAdr = tempAllDest[i].split("@");
            if (!infoDestination.isEmpty()) {
                if (infoDestination.containsKey(tempDestAdr[1])) {
                    infoDestination.get(tempDestAdr[1]).add(tempDestAdr[0]);
                } else {
                    ArrayList<String> tempAL = new ArrayList<>();
                    tempAL.add(tempDestAdr[0]);
                    infoDestination.put(tempDestAdr[1], tempAL);
                }
            } else {
                ArrayList<String> tempAL = new ArrayList<>();
                tempAL.add(tempDestAdr[0]);
                infoDestination.put(tempDestAdr[1], tempAL);
            }
        }
    }

    public void start() {
        this.getInfoDestination();

        // Pour chaque domaine trouvé
        for (Map.Entry<String, ArrayList<String>> entry : infoDestination.entrySet())
        {
            // Si domain connu
            if (this.listDomain.containsKey(entry.getKey())){
                this.initSocket(entry.getKey());
                this.handleDialog(entry.getKey());
            } else {
                 //TODO
//              Ajout mess erreur : domain non connu
            }
        }
        //TODO
        //Afficher toutes les erreurs
    }
    
    /**
     * Recupere et creer la connexion avec le serveur a contacter
     * @param domain - Nom du domaine a contacter
     */
    private void initSocket(String domain){
        String infos = this.listDomain.get(domain);
        
        // Si le domain est connu
        if (infos != null){
            String ip = infos.split(":")[0];
            int port = Integer.parseInt(infos.split(":")[1]);

            try {
                this.socket_server = new Socket(ip, port);
                this.state = ClientState.INIT;
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void handleDialog(String domain){
        BufferedReader inFromServer = null;
        DataOutputStream outToServer = null;
        try {
            inFromServer = new BufferedReader(new InputStreamReader(this.socket_server.getInputStream()));
            outToServer = new DataOutputStream(this.socket_server.getOutputStream());
            
            String messageFromServer;
            String messageForServer = null;
            while (!this.state.equals(ClientState.CLOSED)){
                messageFromServer = inFromServer.readLine();
                //Traitement selon l'etat du client
                switch (this.state){
                    case ClientState.INIT:
                        messageForServer = handleInit(messageFromServer, domain);
                        break;
                    case ClientState.READY:
                        messageForServer = handleReady(messageFromServer);
                        break;
                    case ClientState.MAIL:
                        messageForServer = handleMail(messageFromServer, domain);
                        break;
                    case ClientState.RCPT:
                        messageForServer = handleRcpt(messageFromServer, domain);
                        break;
                    case ClientState.DATA:
                        messageForServer = handleData(messageFromServer, outToServer);
                        break;
                    case ClientState.QUIT:
                        handleQuit(messageFromServer);
                        break;
                }
                //Si le message a envoye nest pas vide
                if (messageForServer != null && !messageForServer.isEmpty()){
                    outToServer.writeBytes(messageForServer);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Ferme les differents flux
                if (inFromServer != null) inFromServer.close();
                if (outToServer!= null) outToServer.close();
                this.socket_server.close();
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
     private String handleInit(String messageFromServer, String domain){
        String response = null;
        if (messageFromServer.startsWith("220")){
            response = "HELO "+domain;
            this.state = ClientState.READY;
        }
        return response;
    }
     
    private String handleReady(String messageFromServer){
        String response = null;
        if (messageFromServer.startsWith("250")){
            response = "MAIL FROM:<"+this.emetteur+">";
            this.state = ClientState.MAIL;
        }
        return response;
    }
      
    private String handleMail(String messageFromServer, String domain){
        String response = null;
        if (messageFromServer.startsWith("250")){
            this.nbDest = this.infoDestination.get(domain).size();
            response = "RCPT TO:<"+this.infoDestination.get(domain).get(nbDest - 1)+">";
            this.state = ClientState.RCPT;
        }
        return response;
    }
      
    private String handleRcpt(String messageFromServer, String domain){
        String response = null;
        if (messageFromServer.startsWith("250") || messageFromServer.startsWith("550")){
            if (messageFromServer.startsWith("550")){
                //TODO
                //MEssage: tel utilisateur existe pas
            }
            this.nbDest--;
            if (this.nbDest > 0){
                response = "RCPT TO:<"+this.infoDestination.get(domain).get(nbDest - 1)+">";
            } else {
                response = "DATA";
                this.state = ClientState.DATA;
            }
        }
        return response;
    }
    
    private String handleData(String messageFromServer, DataOutputStream outToServer) throws IOException{
        String response = null;
        if (messageFromServer.startsWith("354")){
            //Header
            outToServer.writeBytes("From: "+this.emetteur+"<CR><LF>");
            outToServer.writeBytes("To: "+this.destinataires+"<CR><LF>");
            outToServer.writeBytes("Subject: "+this.subject+"<CR><LF>");
            outToServer.writeBytes("<CR><LF>");
            
            //Corps
            String[] lines = this.textMail.split("\n");
            for (String s : lines){
                if (!s.equals(".")){
                    outToServer.writeBytes(s+"<CR><LF>");
                } else {
                    outToServer.writeBytes("<CR><LF>");
                }
            }
            response = ".<CR><LF>";
        } else if (messageFromServer.startsWith("250") || messageFromServer.startsWith("550")){
            response = "QUIT";
            this.state = ClientState.QUIT;
        }
        return response;
    }
    
    private void handleQuit(String messageFromServer){
        if (messageFromServer.startsWith("221")){
           this.state = ClientState.CLOSED;
        }
    }
}
