package server;

import client.Client;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread implements Runnable {

    private Socket socket_client;
    
    private List<String> users;
    
    private List<String> mail;
    
    private String state;
    
    public ServerThread(Socket s) {
        this.socket_client = s;
        System.out.println("New connection (Client: " + this.socket_client.getInetAddress() + ":" + this.socket_client.getPort());
        this.users = new ArrayList<>();
        this.mail = new ArrayList<>();
        this.state = ServerState.INIT;
        (new Thread(this)).start();
    }
  
    @Override
    public void run() {
        BufferedReader inFromClient = null;
        BufferedWriter  outToClient = null;
        try {
            inFromClient = new BufferedReader(new InputStreamReader(this.socket_client.getInputStream()));
            outToClient = new BufferedWriter (new OutputStreamWriter(this.socket_client.getOutputStream()));
            String messageFromClient = null ;
            String messageForClient = null;
            while (!this.state.equals(ServerState.CLOSED)){
                if (!this.state.equals(ServerState.INIT)){
                    messageFromClient = inFromClient.readLine();
                    System.out.println("C:"+messageFromClient);
                }
                //Traitement selon l'etat du server
                switch (this.state){
                    case ServerState.INIT:
                        messageForClient = handleInit();
                        break;
                    case ServerState.LIAISON_FERME:
                        messageForClient = handleLiasonFerme(messageFromClient);
                        break;
                    case ServerState.LAISON_OUVERTE:
                        messageForClient = handleLiasonOuverte(messageFromClient);
                        break;
                    case ServerState.TRANSACTION_DEST:
                        messageForClient = handleTransDest(messageFromClient);
                        break;
                    case ServerState.TRANSACTION_DATA:
                        messageForClient = handleTransData(messageFromClient);
                        break;
                }
                //Si le message a envoye nest pas vide
                if (messageForClient != null && !messageForClient.isEmpty()){
                    System.out.println("S:"+messageForClient);
                    outToClient.write(messageForClient);
                    outToClient.newLine();
                    outToClient.flush();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Ferme les differents flux
                if (inFromClient != null) inFromClient.close();
                if (outToClient != null) outToClient.close();
                System.out.println("Connection terminated (Client: " + this.socket_client.getInetAddress() + ":" + this.socket_client.getPort());
                this.socket_client.close();
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }

    /**
     * Handle the server in its "INIT" state
     * @return  message to send to the client
     */
    private String handleInit (){
        this.state = ServerState.LIAISON_FERME;
        return "220 "+ ServerBis.DOMAIN + " SMT Server Ready";
    }
    
    /**
     * Handle the server in its "LIASON_FERME" state
     * @param messageFromClient - message send by the client
     * @return message to send to the client
     */
    private String handleLiasonFerme (String messageFromClient){
        String response = null;
        if (messageFromClient.equals("HELO "+ServerBis.DOMAIN)){
            response = "250 OK";
            this.state = ServerState.LAISON_OUVERTE;
        }
        return response;
    }
    
    /**
     * Handle the server in its "LIASON_OUVERTE" state
     * @param messageFromClient - message send by the client
     * @return message to send to the client
     */
    private String handleLiasonOuverte (String messageFromClient){
        String response = null;
        if (messageFromClient.startsWith("MAIL FROM:")){
            response = "250 OK";
            initBuffer();
            this.state = ServerState.TRANSACTION_DEST;
        } else if (messageFromClient.equals("QUIT")){
            response = "221 " + ServerBis.DOMAIN + " Closing connection";
            this.state = ServerState.CLOSED;
        }
        return response;
    }
    
    /**
     * Handle the server in its "TRANSACTION_DEST" state
     * @param messageFromClient - message send by the client
     * @return message to send to the client
     */
    private String handleTransDest (String messageFromClient){
        String response = null;
        if (messageFromClient.startsWith("RCPT TO:")){
            // Recupere nom user
            String user = messageFromClient.substring(messageFromClient.indexOf("<") + 1, messageFromClient.indexOf(">")).split("@")[0];
            // Verifie si le user existe sur le server
           if (this.checkUser(user)){
               this.users.add(user);
               response = "250 OK";
           } else {
               response = "550 No such user";
           }
        } else if (messageFromClient.equals("DATA")){
            // S'il y a au moins un utilisateur
            if (!this.users.isEmpty()){
                response = "354 Start mail input; end with <CRLF>.<CRLF>";
                this.state = ServerState.TRANSACTION_DATA;
            } else {
                response = "550 No user";
            }
        } else if (messageFromClient.equals("QUIT")){
            response = "221 " + ServerBis.DOMAIN + " Closing connection";
            this.state = ServerState.CLOSED;
        }
        return response;
    }
    
    /**
     * Handle the server in its "TRANSACTION_DATA" state
     * @param messageFromClient - message send by the client
     * @return message to send to the client
     */
    private String handleTransData(String messageFromClient){
        String response = null;
        if (messageFromClient.equals(".<CR><LF>")){
           response = "250 OK";
           this.mail.add(messageFromClient);
           saveMail();
           this.state = ServerState.LAISON_OUVERTE;
        } else if (messageFromClient.equals("QUIT")){
            response = "221 " + ServerBis.DOMAIN + " Closing connection";
            this.state = ServerState.CLOSED;
        } else {
            this.mail.add(messageFromClient);
        }
        return response;
    }
    
    /**
     * Initialize buffer (clear the list "mail")
     */
    private void initBuffer(){
        this.mail.clear();
    }
    
    /**
     * Check if a user exist in the server 
     * @param user - name of the user
     * @return true if exists, false otherwise
     */
    private boolean checkUser(String user){
        return ServerBis.USERS.contains(user);
    }
    
    /**
     * Save the mail on every necessary user's mailbox
     */
    private void saveMail(){
        FileWriter fw;
        for (String user : this.users){
            try {
                File f = new File("serverFile/" + user + "/MailBox.txt");
                if(f.exists() && !f.isDirectory()) {
                    fw = new FileWriter(f.getPath(),true);
                    for (String s : this.mail){
                        fw.write(s);
                        fw.write("\r\n"); // Saut de ligne
                    }
                    fw.close();
                }else {
                    System.out.println("File not found");
                }
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
}
