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
    static int port = Constants.PORT;//random port for now
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
            multiCastProtocol = new MultiCastProtocol(port, multicastIP);
            runGame(runLobby());
        } else {
            serverIP = input;
            InetAddress hostIP = null;
            try {
                hostIP = InetAddress.getByName(serverIP);
            } catch (Exception exp) {
                exp.printStackTrace();
            }
            uniCastProtocol = new UniCastProtocol(port, timeoutMillis);
            uniCastProtocol.send((new EnCode(me)).getHeader(), hostIP);
            try {
                DeCode deCode = new DeCode(uniCastProtocol.recieve(3, 1400));
                if (deCode.opcode == 1 && deCode.ip != null) {
                    multicastIP = deCode.ip;
                    multiCastProtocol = new MultiCastProtocol(port, multicastIP);
                } else {//server doesnt accept

                }
            } catch (InterruptedIOException exp) {
                exp.printStackTrace();
            }
            runGame((new DeCode(multiCastProtocol.receive(1400))).game);
        }
    }

    public static ClientGame runLobby() {

        ArrayList<Player> tempList = new ArrayList<>();
        tempList.add(me);
        UniCastProtocol lobbyUniCast = new UniCastProtocol(port, timeoutMillis);
        Player newPlayer;
        Player newPlayer2;
        boolean gameStart = false;
        int message = Constants.drawCard;
        while (message == Constants.startGame) {
            try {
                DeCode deCode = new DeCode(lobbyUniCast.recieve(3, 1400));
                newPlayer = deCode.player;
                if (deCode.opcode == 0 && deCode.player != null) {
                    newPlayer = deCode.player;
                    if (tempList.contains(newPlayer) == false) {
                        EnCode enCode = new EnCode(multicastIP);
                        lobbyUniCast.send(enCode.getHeader(), lobbyUniCast.getLastReceivedAddress());
                    }
                }

                deCode = new DeCode(multiCastProtocol.receive(1400));
                if (deCode.opcode == 0 && deCode.player != null) {
                    newPlayer2 = deCode.player;
                    if (newPlayer == newPlayer2) {
                        tempList.add(newPlayer);
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

    public static void startGame(CyclicLinkedList<Player> players) {
        ClientGame game = generateGame(players);
        //send game

    }

    public static void runGame(ClientGame game) {
        Scanner sc = new Scanner(System.in);

        while (game.checkGameRunning()) {
            //receive game

            if (game.checkCurrPlayer()) {
                long startTime = System.nanoTime();
                boolean validMove;
                int message = Constants.timeout;
                boolean validCommand = false;

                System.out.println("Your hand:");
                game.getCurrPlayer().printCards();
                System.out.println("Top card:");
                System.out.println(game.getTopCard().toString());
                System.out.println("Enter the index of the card you would like to play or \"draw\" to draw a card");
                while (System.nanoTime() - startTime < 30 && !validCommand) {
                    if (messages.size() > 0) {
                        message = messages.remove();
                        validCommand = true;
                    }
                }
                //String input = sc.nextLine();
                switch (message) {
                    case Constants.drawCard:
                        Card card = game.drawCard();
                        System.out.println("Card drawn is " + card.toString());
                        System.out.println("Enter \"play\" to play drawn card or n to end turn");
                        input = sc.nextLine();
                        if (input.equals("y")) {
                            PlayerMove playerMove;
                            if (card.getSuit().equals(Suit.Wild)) {
                                playerMove = new PlayerMove(card, resolveWild(sc));
                            } else {
                                playerMove = new PlayerMove(card);
                            }
                            validMove = game.playCard(playerMove);
                        } else {
                            //end turn
                        }
                        break;
                    case Constants.exit:
                    //draw
                    case Constants.timeout:
                    //timedout
                    default:
                    //play card

                }
                if (isNumeric(message)) {
                    PlayerMove playerMove;
                    Card card = game.getCard(Integer.parseInt(input));
                    if (card.getSuit().equals(Suit.Wild)) {
                        playerMove = new PlayerMove(card, resolveWild(sc));
                    } else {
                        playerMove = new PlayerMove(card);
                    }
                    validMove = game.playCard(playerMove);

                } else if (input.equals("draw")) {

                } else {
                    //incorrect command
                    game.drawCard();
                }

                //finish turn
                game.nextTurn();
                //send game
                multiCastProtocol.send((new EnCode(game)).getHeader(), port);

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

    private static Suit resolveWild(Scanner sc) {
        System.out.println("Enter the name of the suit you would like:");
        String input = sc.nextLine();
        Suit suit = null;
        switch (input) {
            case "red":
                suit = Suit.Red;
                break;
            case "green":
                suit = Suit.Green;
                break;
            case "blue":
                suit = Suit.Blue;
                break;
            case "yellow":
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
    }

    private static String parseCommand() {

    }
}

class ClientListener implements Runnable {

    String helpResponse = "Commands:\n\t\"current player\" - returns name of current player\n\t";

    @Override
    public void run() {
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
        }
    }
}
