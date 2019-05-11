/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game.Client;

import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.ArrayList;
import Game.Shared.*;

/**
 * @author alawren3
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    static int timeoutMillis = 100;
    static MultiCastProtocol multiCastProtocol;
    static UniCastProtocol uniCastProtocol;
    static String serverIP;
    static String multicastIP;
    static Player me;
    static Queue<Integer> messages = new ConcurrentLinkedQueue<>();
    static boolean host;
    static ClientGame listenerGame;

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
        System.out.println("Enter the ip of the server (xxx.xxx.xxx.xxx) or \"host\" to host your own game:");
        input = sc.nextLine();
        if (input.equals("host")) {
            System.out.println("Enter the multicast ip:");
            multicastIP = sc.nextLine();
            multiCastProtocol = new MultiCastProtocol(multicastIP);
            runGame(runLobby());
        } else {
            serverIP = input;
            InetAddress hostIP = null;
            try {
                hostIP = InetAddress.getByName(serverIP);
            } catch (Exception exp) {
                exp.printStackTrace();
            }
            uniCastProtocol = new UniCastProtocol();
            uniCastProtocol.send((new EnCode(me)).getHeader(), hostIP);
            try {
                byte[] test = uniCastProtocol.recieve(3, 1400);
                if (test != null) {
                    DeCode deCode = new DeCode(test);
                    if (deCode.opcode == 1 && deCode.ip != null) {
                        multicastIP = deCode.ip;
                        System.out.println("multicast ip:" + multicastIP);
                        multiCastProtocol = new MultiCastProtocol(multicastIP);
                    }
                } else {//server doesnt accept
                    System.out.println("Test is null");
                }
            } catch (InterruptedIOException exp) {
                exp.printStackTrace();
            }
            multiCastProtocol.send((new EnCode(me)).getHeader());
            
            System.out.println("Sent Player object in multicast");
            runGame((new DeCode(multiCastProtocol.receive(1400))).game);
        }
    }

    public static ClientGame runLobby() {
        System.out.println("Running Lobby");
        ArrayList<Player> tempList = new ArrayList<>();
        tempList.add(me);
        UniCastProtocol lobbyUniCast = new UniCastProtocol();
        Player newPlayer;
        Player newPlayer2;
        boolean gameStart = false;
        int message = Constants.drawCard;
        while (message != Constants.startGame) {
            try {
                DeCode deCode = new DeCode(lobbyUniCast.recieve(3, 1400));
                System.out.println("Received Uni Message");
                newPlayer = deCode.player;
                if (deCode.opcode == 0 && deCode.player != null) {
                    newPlayer = deCode.player;
                    if (tempList.contains(newPlayer) == false) {
                        EnCode enCode = new EnCode(multicastIP);
                        lobbyUniCast.send(enCode.getHeader(), lobbyUniCast.getLastReceivedAddress());
                        System.out.println("Sent Multicast IP");
                    }
                }

                deCode = new DeCode(multiCastProtocol.receive(1400));
                System.out.println("Received Multicast Message");
                if (deCode.opcode == 0 && deCode.player != null) {
                    newPlayer2 = deCode.player;
                    if (newPlayer == newPlayer2) {
                        tempList.add(newPlayer);
                        System.out.println("Just Added Player");
                    }
                }

            } catch (InterruptedIOException exp) {
                exp.printStackTrace();
            }

            if (messages.size() > 0) {
                message = messages.remove();

            }
        }
        if (tempList.size() > 1) {
            CyclicLinkedList<Player> playerList = new CyclicLinkedList(tempList);
            return generateGame(playerList);

        }
        return null;
    }

    public static void runGame(ClientGame game) {
        while (game.checkGameRunning()) {
            //receive game

            if (game.checkCurrPlayer()) {
                long startTime = System.nanoTime();
                boolean validMove;
                int message;

                //print out all info
                System.out.println("Enter the index of the card you would like to play or \"draw\" to draw a card");
                message = pollMessages(startTime);
                //String input = sc.nextLine();
                if (message == Constants.drawCard) {
                    Card card = game.drawCard();
                    System.out.println("Card drawn is " + card.toString());
                    System.out.println("Enter \"play\" to play drawn card or \"hold\" to end turn");
                    message = pollMessages(startTime);
                    switch (message) {
                        case Constants.hold:
                            //exit
                            break;
                        case Constants.play:
                            PlayerMove playerMove;
                            if (card.getSuit().equals(Suit.Wild)) {
                                System.out.println("Enter the suit of the card you would like to play (green, blue, red, yellow):");
                                message = pollMessages(startTime);
                                if (message != Constants.timeout) {
                                    playerMove = new PlayerMove(card, resolveSuit(message));
                                } else {
                                    playerMove = null;
                                }
                            } else {
                                playerMove = new PlayerMove(card);
                            }
                            validMove = game.playCard(playerMove);//will handle null as false
                            break;
                        case Constants.timeout:
                            //exit
                            break;
                        default:
                            //incorrect command, exit
                            break;
                    }
                } else if (message >= Constants.minIndex) {
                    PlayerMove playerMove;
                    Card card = game.getCard(message);
                    if (card.getSuit().equals(Suit.Wild)) {
                        System.out.println("Enter the suit of the card you would like to play (green, blue, red, yellow):");
                        message = pollMessages(startTime);
                        if (message != Constants.timeout) {
                            playerMove = new PlayerMove(card, resolveSuit(message));
                        } else {
                            playerMove = null;
                        }
                    } else {
                        playerMove = new PlayerMove(card);
                    }
                    validMove = game.playCard(playerMove);//will handle null as false
                } else if (message == Constants.exit) {
                    //disconnect
                } else if (message == Constants.timeout) {
                    game.drawCard();
                } else {
                    game.drawCard();
                }

                //finish turn
                game.nextTurn();
                //send game
                multiCastProtocol.send((new EnCode(game)).getHeader());

            } else {
                System.out.println(game.getTopCard().toString() + " is top card");
                //wait for other players turn

                //receive game
                DeCode deCode = new DeCode(multiCastProtocol.receive(1400));
                game = deCode.game;
            }
        }
        System.out.println(game.getWinner().getName() + " won");
        //kill thread
    }

    private static int pollMessages(long startTime) {
        boolean validCommand = false;
        int message = Constants.timeout;
        while (System.nanoTime() - startTime < Constants.timeoutNanos && message == Constants.timeout) {
            if (messages.size() > 0) {
                message = messages.remove();
            }
        }
        return message;
    }

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

    private static ClientGame generateGame(CyclicLinkedList<Player> players) {
        if (host) {
            int multiple = players.size() > 5 ? 4 : 2;
            return new ClientGame(players, multiple);
        }
        return null;
    }

}

class ClientListener implements Runnable {

    String helpResponse = "Commands:\n\t\"current player\" - returns name of current player\n\t";

    @Override
    public void run() {/*
        Scanner sc = new Scanner(System.in);
        System.out.println("Hey, I'm Robo Rob!");
        System.out.println("The main method is busy right now, so I'll be processing your requests :)");
        while (true) {
            String message = sc.nextLine();
            switch (message) {
                case "help":
                    System.out.println(helpResponse);
                    break;
                case "current player":
                    System.out.println(ClientGame.getCurrPlayer().getName());
                    break;
                case "current player hand size":
                    System.out.println(ClientGame.getCurrPlayerHandSize());
                    break;
                case "my cards":
                    ClientGame.getMyPlayer().printCards();
                    break;
                case "my hand size":
                    System.out.println(ClientGame.getMyPlayer().getHandLength());
                    break;
                case "top card":
                    System.out.println(ClientGame.getTopCard().toString());
                    break;
                case "play card":
                    ClientGame.getMyPlayer().printCards();
                    System.out.println("Enter the index of the card you would like to play");
                    String cardIndex = sc.nextLine();
                    Main.messages.add(cardIndex);
                    break;
                default:
                    System.out.println("Sorry I don't know what your talking about, type \"help\" for the list of commands");
                    break;
            }
        }*/
    }
}
