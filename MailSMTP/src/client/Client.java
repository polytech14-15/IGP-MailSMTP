package client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Client {

    private final String destinataires;
    private final Map<String, String> listDomain;
    private Map<String, ArrayList<String>> infoDestination;

    public Client(String destinataires) {
        this.listDomain = new HashMap<String, String>();
        this.listDomain.put("toto.fr", "127.0.0.1:2009");
        this.destinataires = destinataires;
        this.infoDestination = new HashMap<String, ArrayList<String>>();
    }

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

    }
}
