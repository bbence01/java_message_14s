package hu.petrik.chatlib.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A chat szerveren belül egy kliens kapcsolatot kezel.
 */
class Client {
    private Socket socket;
    private ChatServer server;
    
    private Object syncRoot = new Object();
    private OutputStreamWriter writer;
    
    String nickname;
    private static Random random = new Random();
    
    
     // Idő hozzáadás
    //lekéri a jelenlegi időt
    // megformázza a jelenlegi időt 
    private  Timestamp timestamp = new Timestamp(System.currentTimeMillis()); 
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); 
    
    // Command lista
     List<String> commands = Arrays.asList(
             "/q = kilépés", 
             "/nick = névváltozattás",
             "/list = felhasználok listája",
             "/com = parancsok listája", 
             "/whisper = üzenet egy kiválasztott felhasználónak",
             "/kick = felhasználó kirugása"  ); 
    
    /**
     * Létrehoz egy kliens objektumot a megadott socket-hez.
     * 
     * @param socket A kapcsolat a klienshez.
     * @param server A szerver objektum, amely létrehozta a klienst.
     */
    public Client(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        this.nickname = "User #" + random.nextLong();
    }
    
    /**
     * Fogadja a kliens által küldött üzeneteket, és továbbítja a szervernek.
     * A függvény blokkoló. Csak egyszer hívjuk meg!
     * Ha kivételt dob, feltételezhetjük, hogy a kliens kapcsolat használhatatlan.
     * 
     * @throws IOException 
     */
    public void start() throws IOException {
        writer = new OutputStreamWriter(socket.getOutputStream());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] darabolt = line.split(" ", 2);
                
                if (darabolt.length == 0) {
                    continue;
                }
                
                String parancs = darabolt[0];
                String parameter = "";
                if (darabolt.length != 1) {
                    parameter = darabolt[1];
                }
                
                switch (parancs) {
                    
                    
                    case "/q":
                        server.send(" [ " + sdf.format(timestamp) + " ] " + "*** "+ nickname + " :  'Kilépett'  " + line + "\n");
                        socket.close();
                        return;
                        
                    case "/nick":
                        String ujNicknev = parameter.trim();
                        if (!ujNicknev.equals("")) {
                            server.send("* " + nickname + " új nickneve " + ujNicknev + "\n");
                            nickname = ujNicknev;
                        }
                        
                        
                         break ;
                         
                    case "/list":
                        
                    
                         for(int i=0; i<server.clients.size(); i++ )
                          {
         
                              server.send( "list" + " "+ "/whisper" + " " + nickname + "  " + server.clients.get(i).nickname + "\n");
                              
                          }
                        
                            
                        break;
                        
                        case "/com":
                            
                         for(int i=0; i<commands.size();i++ )
                          {
         
                              server.send("  " +" /whisper " + nickname + " ' "  + nickname + " " + commands.get(i) +" " + " \n ");
                              
                              }
                         
                            break;
                            
                            
                        case "/slap":
                            String slaped = parameter.trim();
                               server.send(" At  [ " + sdf.format(timestamp) + " ]  " + nickname + " slaps " + slaped + " around a bit with a large trout  " + "\n");
                            
                            
                            break;
                                
                    
                        
                        
                    default:
                        server.send(" [ " + sdf.format(timestamp) + " ] "+ nickname + ": " + line + "\n");
                        break;
                }
            }
        }
    }
    
    /**
     * Elküldi a megadott üzenetet a kliensnek.
     * A függvény szálbiztos.
     * 
     * @param message
     * @throws IOException 
     */
    void send(String message) throws IOException {
        synchronized(syncRoot) {
            writer.write(message);
            writer.flush();
        }
    }
    
    /**
     * Leállítja a szervert (a hatására a {@link #start()} függvény kivételt dob).
     * A függvény szálbiztos.
     * 
     * @throws IOException 
     */
    public void stop() throws IOException {
        socket.close();
    }
}
