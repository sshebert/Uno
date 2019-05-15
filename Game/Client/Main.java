//multiple authors
//Main was debugged by everyone

package Game.Client;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.ArrayList;
import Game.Shared.*;

public class Main {

    /**
     * @param args the command line arguments
     */
    static int timeoutMillis = 100;
    static MultiCastProtocol multiCastProtocol;
    static UniCastProtocol uniCastProtocol;
    private static Constants C = new Constants();
    static String serverIP;
    static String multicastIP;
    static String key;
    static Player me;
    static ConcurrentLinkedQueue<Integer> messages = new ConcurrentLinkedQueue<>();
    static boolean host;
    static ClientGame listenerGame;
    static Thread listenerThread;
    static boolean printedPlayersTurn = false;
    static String filePath = "..\\unoSave.txt";
    static boolean listenerRequestedExit = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your name:");
        String input = sc.nextLine();
        try {
            me = new Player(input, InetAddress.getLocalHost());
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        System.out.println("Type \"load\" to load into game or \"lobby\" to join/host a lobby");
        if (sc.nextLine().equals("load")) {
            String[] data = readFromDisk();
            if(data != null){
                key = data[0];
                multicastIP = data[1];
                multiCastProtocol = new MultiCastProtocol(multicastIP);
                ClientGame game = new DeCode(multiCastProtocol.receive(7000,60000)).game;
                if(game == null){
                    System.out.println("Game is no longer active");
                }else{
                    ClientListener clientListener = new ClientListener(messages, multiCastProtocol);
                    listenerThread = new Thread(clientListener);
                    listenerThread.start();
                    runGame(game);
                }
            }else{
                System.out.println("Save file does not exist");
            }
        } else {
            //key = generatePrivateKey(16);
            System.out.println("Enter the ip of the server (xxx.xxx.xxx.xxx) or \"host\" to host your own game:");
            input = sc.nextLine();

            if (input.equals("host")) {
                System.out.println("Enter the multicast ip:");
                multicastIP = sc.nextLine();
                //start work by Benjamin Groman
                boolean ipWorks = false;
                while (!ipWorks) {
                    try {
                        if (InetAddress.getByName(multicastIP).isMulticastAddress()) {
                            ipWorks = true;
                        } else {
                            System.out.println("Invalid address. Please enter the multicast ip:");
                            multicastIP = sc.nextLine();
                        }
                    } catch (UnknownHostException e) {
                        //this is handled the same as a non-multicast address
                        System.out.println("Invalid address. Please enter the multicast ip:");
                        multicastIP = sc.nextLine();
                    }
                }
                //end work by Benjamin Groman
                multiCastProtocol = new MultiCastProtocol(multicastIP);
                //start work by Benjamin Groman
                System.out.println("Enter a 16 character key:");
                input = sc.nextLine();
                boolean goodKey = validKey(input, 16);
                while (!goodKey) {
                	System.out.println("That key is not valid. Please try again:");
                	input = sc.nextLine();
                	goodKey = validKey(input, 16);
                }
                key = input;
                //end work by Benjamin Groman
                System.out.println("Tell all your friends the key is: " + key);//by Benjamin Groman
                if (saveToDisk(key, multicastIP)) {
                    System.out.println("Info saved to disk");
                } else {
                    System.out.println("Failed to save to disk");
                }
                ClientListener clientListener = new ClientListener(messages, multiCastProtocol);
                listenerThread = new Thread(clientListener);
                listenerThread.start();
                runGame(runLobby());
            } else {
                serverIP = input;
                InetAddress hostIP = null;
                try {
                    hostIP = InetAddress.getByName(serverIP);
                } catch (Exception exp) {
                	//start work by Benjamin Groman
                	System.out.println("Invalid address. Terminating program.");
                	return;
                	//end work by Benjamin Groman
                    //exp.printStackTrace();
                }
                //start work by Benjamin Groman
                System.out.println("Enter the host's key:");
                input = sc.nextLine();
                boolean goodKey = validKey(input, 16);
                while (!goodKey) {
                	System.out.println("That key is not possibly valid. Please try again:");
                	input = sc.nextLine();
                	goodKey = validKey(input, 16);
                }
                key = input;
                //end work by Benjamin Groman
                uniCastProtocol = new UniCastProtocol();
                uniCastProtocol.send((new EnCode(me)).getHeader(), hostIP);
                //System.out.println("Sent unicast request");
                try {
                    byte[] test = uniCastProtocol.recieve(20, 1400);
                    if (test != null) {
                        DeCode deCode = new DeCode(test);
                        if (deCode.opcode == 1 && deCode.ip != null) {
                            multicastIP = deCode.ip;
                            //System.out.println("multicast ip:" + multicastIP);
                            multiCastProtocol = new MultiCastProtocol(multicastIP);

                            if (saveToDisk(key, multicastIP)) {
                                System.out.println("Info saved to disk");
                            } else {
                                System.out.println("Failed to save to disk");
                            }
                        }
                    } else {//server doesnt accept
                        System.out.println("Test is null");
                        return;//we should probably just exit here - Benjamin Groman
                    }
                } catch (InterruptedIOException exp) {
                    exp.printStackTrace();
                }
                multiCastProtocol.send((new EnCode(me)).getHeader());

                //System.out.println("Sent Player object in multicast");
                long start = System.nanoTime();
                System.out.println("Waiting for messages");
                while (System.nanoTime() - start < 12000000000L && !listenerRequestedExit) {
                    //System.out.println(System.nanoTime() - start);
                    byte[] tempbyte = multiCastProtocol.receive(7000, C.MultiTimeout);
                    if (tempbyte != null) {
                        DeCode dec = new DeCode(tempbyte);
                        if (dec.opcode == 3) {
                            //System.out.println("Received keep alive");
                            start = System.nanoTime();
                        } else if (dec.opcode == 2) {
                            ClientListener clientListener = new ClientListener(messages, multiCastProtocol);
                            listenerThread = new Thread(clientListener);
                            listenerThread.start();
                            runGame(dec.game);
                            System.exit(0);
                        }
                    }
                }
                //start work by Benjamin Groman
                if (listenerRequestedExit) {
                	System.out.println("Server lost. Terminating.");
                }
                else {
                	System.out.println("Terminating as requested.");
                }
                System.exit(0);
                //end work by Benjamin Groman
            }
        }
    }

    public static ClientGame runLobby() {
        System.out.println("Running Lobby");
        ArrayList<Player> tempList = new ArrayList<>();
        tempList.add(me);
        UniCastProtocol lobbyUniCast = new UniCastProtocol();
        Player newPlayer = null;
        Player newPlayer2;
        long time = System.nanoTime();
        int message = C.drawCard;
        while (message != C.startGame) {
        	//start work by Benjamin Groman
        	if (message == C.exit || listenerRequestedExit) {
        		System.out.println("Terminating as requested.");
        		System.exit(0);
        	}
        	//end work by Benjamin Groman
            try {
                if (System.nanoTime() - time > (C.timeoutNanos / 6L)) {
                    time = System.nanoTime();
                    multiCastProtocol.send((new EnCode(3)).getHeader());
                    //System.out.println("Just sent keep alive");
                }

                byte[] temp = lobbyUniCast.recieve(3, 1400);
                if (temp != null) {
                    DeCode deCode = new DeCode(temp);
                    newPlayer = deCode.player;
                    if (deCode.opcode == 0 && deCode.player != null) {
                        newPlayer = deCode.player;
                        System.out.println(newPlayer.getName());
                        if (tempList.contains(newPlayer) == false) {
                            EnCode enCode = new EnCode(multicastIP);
                            lobbyUniCast.send(enCode.getHeader(), lobbyUniCast.getLastReceivedAddress());
                            //System.out.println("Sent Multicast IP");
                        }
                    }
                }

                temp = multiCastProtocol.receive(1400, C.MultiTimeout);
                if (temp != null) {
                    DeCode deCode = new DeCode(temp);
                    //System.out.println("Received Multicast Message");
                    if (deCode.opcode == 0 && deCode.player != null) {
                        //System.out.println("correct opcode");
                        newPlayer2 = deCode.player;
                        if (newPlayer.getInetAddress().equals(newPlayer2.getInetAddress())) {
                            //System.out.println("equals previous player");
                            tempList.add(newPlayer);
                            System.out.println("Just Added Player " + newPlayer.getName());
                        }
                    }
                }

            } catch (InterruptedIOException exp) {
                //exp.printStackTrace();
            }

            if (messages.size() > 0) {
                message = messages.remove();

            }
        }

        //System.out.println(tempList.size());
        if (tempList.size() > 0) {
            //System.out.println(tempList.size());
            CyclicLinkedList<Player> playerList = new CyclicLinkedList(tempList);
            ClientGame game = generateGame(playerList);
            EnCode enCode = new EnCode(game);
            //System.out.println("Game Size: " + enCode.getHeader().length );
            multiCastProtocol.send((new EnCode(game).getHeader()));
            return game;

        }
        return null;
    }

    public static void printInfo(ClientGame game) {
        //System.out.println(game.getTopCard().toString() + " is top card");
        game.getCurrPlayer().printCards();
        //print out all players cards
    }
    
    //author Sam Shebert
    public static void runGame(ClientGame game) {
        //System.out.println(game.getCurrPlayer().getName() + " me: " + me.getName());
        while (game.checkGameRunning() && !listenerRequestedExit) {//check game is still running
            //receive game
            if (!printedPlayersTurn) {//prevents printing a game update when multiple of the same multicast are received
                System.out.println(game.getCurrPlayer().getName() + "'s turn");
                System.out.println(game.getTopCard().toString() + " is top card");
            }
            if (game.checkCurrPlayer()) {//check if it is this clients turn
                game.getCurrPlayer().setKill(false);//reset your TTL
                if(game.getCurrPlayer().getHandLength() >= 2){//reset uno call from last turn
                    game.getCurrPlayer().setUnoSafe(false);
                }
                boolean skip = game.checkSkip();//check if you are being skipped
                //System.out.println(skip);
                if (!skip) {//if not skipped, do turn
                    long startTime = System.nanoTime();//start timer
                    boolean validMove;
                    int message;

                    //print out all info
                    printInfo(game);
                    //System.out.println(game.getTopCard().toString() + " is top card");
                    //ame.getCurrPlayer().printCards();
                    System.out.println("Enter the index of the card you would like to play or \"draw\" to draw a card");
                    message = pollMessages(startTime, game);//checks user messages/timer
                    //String input = sc.nextLine();
                    if (message == C.exit){
                        listenerThread.interrupt();
                        return;
                    }
                    if (message == C.drawCard) {//player types draw
                        Card card = game.drawCard();
                        System.out.println("Card drawn is " + card.toString());
                        System.out.println("Enter \"play\" to play drawn card or \"hold\" to end turn");//give player option to play drawed card
                        message = pollMessages(startTime, game);
                        switch (message) {
                            case Constants.hold:
                                //exit
                                break;
                            case Constants.play:
                                PlayerMove playerMove;
                                if (card.getSuit().equals(Suit.Wild)) {//check wild case, will require a suit to be inputed
                                    System.out.println("Enter the suit of the card you would like to play (green, blue, red, yellow):");
                                    message = pollMessages(startTime, game);
                                    if (message != C.timeout) {
                                        playerMove = new PlayerMove(card, resolveSuit(message));
                                    } else {
                                        playerMove = null;
                                    }
                                } else {
                                    playerMove = new PlayerMove(card);
                                }
                                validMove = game.playCard(playerMove);//will handle null as false
                                break;
                            case Constants.timeout://if player runs out of turn here, don't make them draw another card just end turn
                                System.out.println("Out of time");
                                break;
                            default:
                                //incorrect command, exit
                                break;
                        }
                    } else if (message >= C.minIndex) {//player trying to play card
                        PlayerMove playerMove;
                        Card card = game.getCard(message);
                        //System.out.println("trying to play " + card.toString());
                        if (card.getSuit().equals(Suit.Wild)) {//check wild case, will require a suit to be inputed
                            System.out.println("Enter the suit of the card you would like to play (green, blue, red, yellow):");
                            message = pollMessages(startTime, game);
                            if (message != C.timeout) {
                                playerMove = new PlayerMove(card, resolveSuit(message));
                            } else {
                                System.out.println("Out of time");
                                playerMove = null;
                            }
                        } else {
                            playerMove = new PlayerMove(card);
                        }
                        validMove = game.playCard(playerMove);//will handle null as false
                        if (!validMove) {//if player did not play a valid card or runs out of time here, draw card
                            card = game.drawCard();
                            System.out.println("Card drawn is " + card.toString());
                        }
                    } else if (message == C.exit || listenerRequestedExit) {
                        //disconnect
                    	//this needs to do something! - Benjamin Groman
                    	System.exit(0);
                    } else if (message == C.timeout) {//if player runs out of time here, draw card
                        System.out.println("Out of time");
                        game.drawCard();
                    } else {
                        game.drawCard();
                    }

                    //finish turn
                    
                    game.nextTurn();//increment game state so next player will know it is their turn
                    //send game
                    //System.out.println("just sent game");
                    multiCastProtocol.send((new EnCode(game)).getHeader());
                } else {//if you were skipped
                    Card[] cards = game.getSkipCards();//check if you need to draw cards (for draw 2/wild draw)
                    if (cards != null) {
                        for (Card card : cards) {
                            System.out.println("Skip draw card: " + card.toString());
                        }
                    }
                    game.nextTurn();//end your turn and send game
                    multiCastProtocol.send((new EnCode(game)).getHeader());
                }
                System.out.println();

            } else {//not your turn
                //wait for other players turn

                //receive game
                byte[] gameData = multiCastProtocol.receive(7000, 30000);// will return null if timeout

                if (multiCastProtocol.getLastReceivedAddress().equals(me.getInetAddress())) {//prevents printing game state multiple times when receiving your own multicast packet
                    //received my own game packet
                    //System.out.println("Just received my own game packet");
                    printedPlayersTurn = true;
                } else if (gameData != null) {//if received something
                    DeCode deCode = new DeCode(gameData);
                    if(deCode.opcode == 2) {// received game
                        System.out.println();
                        printedPlayersTurn = false;
                        //System.out.println("just received game");
                        game = deCode.game;
                    }else if(deCode.opcode == 4){//receieved uno
                        System.out.println("Uno called");
                        printedPlayersTurn = true;
                        //do nothing
                    }//if did not receive packet with opcode 2 or 4 then do nothing
                } else {//current player disconnected/died
                    if(game.getCurrPlayer().getKill()){//check there TTL
                        game.removePlayer(game.getCurrPlayer());//if TTL is up, remove from game
                    }else{
                        game.getCurrPlayer().setKill(true);//if TTL is not up, set TTL to 0
                    }
                    game.nextTurn();//increment the game state, now next player can take over
                    multiCastProtocol.send((new EnCode(game)).getHeader());
                }
            }

        }
        //start work by Benjamin Groman
        //stop a winner from getting declared on your end when you quit
        if (listenerRequestedExit) {
        	System.out.println("Terminating as requested.");
        	System.exit(0);
        }
        //end work by Benjamin Groman
        System.out.println(game.getWinner().getName() + " won");
        listenerThread.interrupt();
        return;
        //kill thread
    }
    
    //author Sam Shebert
    //polls listener thread for user inputs
    //also keeps track of the players turn duration and receiving uno packets
    //if player runs out of time, the game will still send a multicast signalling that client is still alive and the turn is over
    //this is different than if the client dies, as then it will not send anything signalling the end of the turn
    private static int pollMessages(long startTime, ClientGame game) {
        int message = C.timeout;
        while (System.nanoTime() - startTime < C.timeoutNanos && message == C.timeout) {
            if (messages.size() > 0) {
                message = messages.remove();
            }
            byte[] unoCheck = multiCastProtocol.receive(7000,10);
            if(unoCheck != null){
                DeCode d = new DeCode(unoCheck);
                if(d.opcode == 4){
                    if(game.resolveUno(multiCastProtocol.getLastReceivedAddress())){
                        System.out.println("Uno was legit");
                    }else{
                        System.out.println("Uno was not legit");
                    }
                }
            }
        }
        return message;
    }
    
    //author Sam Shebert
    private static Suit resolveSuit(int input) {
        Suit suit = null;
        switch (input) {
            case Constants.red:
                suit = Suit.Red;
                break;
            case Constants.green:
                suit = Suit.Green;
                break;
            case Constants.blue:
                suit = Suit.Blue;
                break;
            case Constants.yellow:
                suit = Suit.Yellow;
                break;
        }
        return suit;
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }
    
    //author Sam Shebert
    private static ClientGame generateGame(CyclicLinkedList<Player> players) {
        int multiple = players.size() > 5 ? 2 : 1;
        return new ClientGame(players, multiple);
    }

    //author Sam Shebert
    private static String generatePrivateKey(int length){
        int lowerBound = 48;
        int upperBound = 122;
        Random random = new Random();
        StringBuilder buf = new StringBuilder(length);
        for(int i = 0; i < length; i++){
            int randomBoundedInt = lowerBound + (int)(random.nextFloat() * (upperBound - lowerBound + 1));
            buf.append((char)randomBoundedInt);
        }
        return buf.toString();
    }
    /**This function determines whether a given string could have been the key generated by the host,
     * but does not guarantee that the given string is, in fact, that key.
     * @author Benjamin Groman
     * @param k The key to check
     * @param len The length of key expected
     * @return Whether k could possibly be returned by generatePrivateKey(len)
     */
    private static boolean validKey(String k, int len) {
    	if (k.length() != len) return false;
    	for (char c : k.toCharArray()) {
    		if (c < 48 || c > 122 || c == 92) return false;
    	}
    	return true;
    }
    public static String getKey() {
    	return key;
    }

    //author Sam Shebert
    private static boolean saveToDisk(String key, String multicastip){
        File file = new File(filePath);
        if(file.exists()){
            file.delete();
        }
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(key);
            fileWriter.write(System.lineSeparator());
            fileWriter.write(multicastip);
            fileWriter.close();
        }catch (IOException exp){
            return false;
        }
        return true;
    }

    //author Sam Shebert
    private static String[] readFromDisk(){
        File file = new File(filePath);
        if(!file.exists()){
            return null;
        }
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String key = bufferedReader.readLine();
            String multicastip = bufferedReader.readLine();
            bufferedReader.close();
            String[] out = {key,multicastip};
            return out;
        }catch (IOException exp) {
            return null;
        }
    }
}
