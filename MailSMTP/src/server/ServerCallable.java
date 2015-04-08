/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author p1305728
 */
public class ServerCallable implements Callable<Socket>{
    Socket socket_client;
    Statut statut;
    List<String> adresseDestinataires;    
    String corpsMail;
    String sujet;

    private void initBuffer() {
        adresseDestinataires = new ArrayList<String>();
        corpsMail = "";
        sujet = "";        
    }


    
    private enum Statut {
        LIAISON_FERME, LAISON_OUVERTE, TRANSACTION_DEST, TRANSACTION_DATA
    };

    public ServerCallable(Socket socket_client){
        this.socket_client = socket_client;
        statut = Statut.LIAISON_FERME;        
    }

    @Override
    public Socket call() throws Exception {
        int client_port = socket_client.getPort();
        System.out.println("Nouvelle entrée : " + client_port);
        
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket_client.getInputStream()));
        DataOutputStream outToClient = new DataOutputStream(socket_client.getOutputStream());
        
        String clientSend;
        String clientRequest = null;

        System.out.println("Send client "+ client_port +" : 220 toto.fr SMT Service Ready");
        clientSend = "220 toto.fr SMT Service Ready";        
        outToClient.writeBytes(clientSend);
        
        //On lit le HELO
        clientRequest = inFromClient.readLine();
        System.out.println("Receive client "+ client_port +" : "+clientRequest);
        while(!clientRequest.startsWith("HELO")){
            clientRequest = inFromClient.readLine();
            System.out.println("Receive client "+ client_port +" : "+clientRequest);
        }
        //On a reçu le HELO
        outToClient.writeBytes("250 OK");
        System.out.println("Send client "+ client_port +" : 250 OK");
        statut = Statut.LAISON_OUVERTE;
        
        while (statut != Statut.LIAISON_FERME) {
            //On lit le MAIL FROM
            clientRequest = inFromClient.readLine();
            System.out.println("Receive client "+ client_port +" : "+clientRequest);
            while(!clientRequest.startsWith("MAIL FROM")){
                clientRequest = inFromClient.readLine();
                System.out.println("Receive client "+ client_port +" : "+clientRequest);
            }
            //On a reçu le MAIL FROM
            outToClient.writeBytes("250 OK");
            System.out.println("Send client "+ client_port +" : 250 OK");
            statut = Statut.TRANSACTION_DEST;
            initBuffer();

            //On lit le mot suivant
            clientRequest = inFromClient.readLine();
            System.out.println("Receive client "+ client_port +" : "+clientRequest);
            String request[] = clientRequest.split(" ");
            switch(request[0]){
                case "RCPT":
                    //TODO
                    break;
                case "DATA":
                    //TODO
                    break;
                case "QUIT":
                    //TODO
                    break;
            }      
        }  
        return socket_client;
    }
    
}
