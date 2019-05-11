/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game.Client;

import java.io.InterruptedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import Game.Shared.*;

/**
 *
 * @author alawren3
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    static int port = 2705;//random port for now
    static int timeoutMillis = 100;
    static MultiCastProtocol multiCastProtocol;
    static UniCastProtocol uniCastProtocol;
    static String serverIP;
    static String multicastIP;
    static Player me;
    static Queue<String> messages = new ConcurrentLinkedQueue<>();
    static boolean host;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the ip of the server (xxx.xxx.xxx.xxx):");
        serverIP = sc.nextLine();
        uniCastProtocol = new UniCastProtocol(port,serverIP,timeoutMillis);
        uniCastProtocol.send((new EnCode(0)).getHeader());
        try {
            DeCode deCode = new DeCode(uniCastProtocol.recieve(3, 1400));
            if(deCode.opcode == 1 && deCode.ip != null){
                multicastIP = deCode.ip;
                multiCastProtocol = new MultiCastProtocol(port,multicastIP);
            }else{//server doesnt accept

            }
        }catch (InterruptedIOException exp){
            exp.printStackTrace();
        }
        multiCastProtocol = new MultiCastProtocol(port);
        //runGame(runLobby());
    }

    public static CyclicLinkedList<Player> runLobby(){

    }

    public static void startGame(CyclicLinkedList<Player> players){
        ClientGame game = generateGame(players);
        //send game

    }

    public static void runGame(ClientGame game){
        Scanner sc = new Scanner(System.in);

        while(game.checkGameRunning()){
            //receive game


            if(game.checkCurrPlayer()){
                boolean validMove;
                System.out.println("Your hand:");
                game.getCurrPlayer().printCards();
                System.out.println("Top card:");
                System.out.println(game.getTopCard().toString());
                System.out.println("Enter the index of the card you would like to play or \"draw\" to draw a card");
                String input = sc.nextLine();
                if(isNumeric(input)){
                    PlayerMove playerMove;
                    Card card = game.getCard(Integer.parseInt(input));
                    if(card.getSuit().equals(Suit.Wild)){
                        playerMove = new PlayerMove(card,resolveWild(sc));
                    }else{
                        playerMove = new PlayerMove(card);
                    }
                    validMove = game.playCard(playerMove);

                }else if(input.equals("draw")){
                    Card card = game.drawCard();
                    System.out.println("Card drawn is " + card.toString());
                    System.out.println("Enter y to play drawn card or n to end turn");
                    input = sc.nextLine();
                    if(input.equals("y")){
                        PlayerMove playerMove;
                        if(card.getSuit().equals(Suit.Wild)){
                            playerMove = new PlayerMove(card,resolveWild(sc));
                        }else{
                            playerMove = new PlayerMove(card);
                        }
                        validMove = game.playCard(playerMove);
                    }else{
                        //end turn
                    }
                }else{
                    //incorrect command
                    game.drawCard();
                }

                //finish turn
                game.nextTurn();
                //send game
                multiCastProtocol.send((new EnCode(game, 1)).getHeader(),port );

            }else{
                System.out.println(game.getTopCard().toString() + " is top card");
                //wait for other players turn

                //receive game
                DeCode deCode = new DeCode(multiCastProtocol.receive(1400));
                game = deCode.game;
            }
        }
        System.out.println(game.getWinner().getName() + " won");
    }

    private static Suit resolveWild(Scanner sc){
        System.out.println("Enter the name of the suit you would like:");
        String input = sc.nextLine();
        Suit suit = null;
        switch (input){
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


    private static ClientGame generateGame(CyclicLinkedList<Player> players){
        if(host) {
            int multiple = players.size() > 5 ? 4 : 2;
            return new ClientGame(players, multiple);
        }
    }


}

class ClientListener implements Runnable{

    String helpResponse = "Commands:\n\t\"current player\" - returns name of current player\n\t";

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Hey, I'm Robo Rob!");
        System.out.println("The main method is busy right now, so I'll be processing your requests :)");
        while(ClientGame.getGameRunning()){
            String message = sc.nextLine();

            switch (message){
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
