package Game.Client;

import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;


import Game.Shared.Constants;
import Game.Shared.EnCode;

/**
 * Handles the gathering and basic filtering of user input.
 * @author Benjamin Groman
 *
 */
class ClientListener implements Runnable {
    private static Constants C = new Constants();
    ConcurrentLinkedQueue queue;
    MultiCastProtocol multiCastProtocol;
	/*for debugging
	 *testers should also uncomment the Main class at the bottom of the file 
	public static void main(String[] args) {
		new ClientListener().run();
	}
	*/
	ClientListener(ConcurrentLinkedQueue q, MultiCastProtocol mcp){
	    queue=q; multiCastProtocol=mcp;
    }
	/**
	 * Listens for user input, filters out obviously bad inputs,
	 * and sends the remainder to the main thread.
	 */
    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        boolean exitRequested = false;
        while (!exitRequested) {
            String message = sc.nextLine();
            //using else-ifs rather than switch statement to allow ignoring case
            //for printing out the available commands
            if (message.equalsIgnoreCase("help")) {
            	printHelpResponse();
            }else if(queue.size() == 0) {
                //for terminating the client
                if (message.equalsIgnoreCase("exit")) {
                    queue.add(C.exit);
                    exitRequested = true;
                }
                else if(message.equals("uno")){
                    multiCastProtocol.send(new EnCode(4).getHeader());
                }
                //for drawing a card
                else if (message.equalsIgnoreCase("draw")) {
                    queue.add(C.drawCard);
                }
                //for declaring the color after playing a wild
                else if (message.equalsIgnoreCase("blue")) {
                    queue.add(C.blue);
                } else if (message.equalsIgnoreCase("red")) {
                    queue.add(C.red);
                } else if (message.equalsIgnoreCase("green")) {
                    queue.add(C.green);
                } else if (message.equalsIgnoreCase("yellow")) {
                    queue.add(C.yellow);
                }
                //for selecting whether to attempt to play the card that was just picked up
                else if (message.equalsIgnoreCase("play")) {
                    queue.add(C.play);
                } else if (message.equalsIgnoreCase("hold")) {
                    queue.add(C.hold);
                }
                //for starting the game
                else if (message.equalsIgnoreCase("start") || message.equalsIgnoreCase("start game")) {
                    queue.add(C.startGame);
                }
                //positive integers for selecting a card to play
                //cap the size at three digits to prevent overflows in parsing
                //having a 100 cards is possible if two decks are combined, but rare
                //second check screens values below the minimum index, which could be negative for all this class cares
                else if (message.matches("-?\\d{1,3}") && Integer.parseInt(message) >= C.minIndex) {
                    queue.add(Integer.parseInt(message));
                }
                //bad input provided
                else {
                    System.out.println("Command unrecognized. Type help for a full list of commands.");
                }
            }else{
                System.out.println("Queue is full, wait please");
            }
        }
    }
    /**
     * Prints the help message, listing all available commands.
     */
    private void printHelpResponse() {
    	System.out.println("You have " + (C.timeoutNanos / 1000000000) + " seconds for your turn.");
    	System.out.println("Commands:");
    	System.out.println("start, start game - start the game if you are the host");
    	System.out.println("help - print this message");
    	System.out.println("exit - quit the game");
    	System.out.println("uno - attempt to call someone out when they have just one card");
    	System.out.println("draw - draw a card");
    	System.out.println("play - play the card that was just drawn");
    	System.out.println("hold - don't play the card that was just drawn");
    	System.out.println("red, blue, green, yellow - select a color after playing a wild card");
    	String indices = "";
    	for (int i = C.minIndex; i < C.minIndex + 3; i++) {
    		indices += i + ", ";
    	}
    	System.out.println(indices + "... - play a card from your hand");
    }
    /*
    //for debugging
    private static class Main {
    	private static class messages {
    		private static void add(Integer i ) {
    			System.out.println(i);
    		}
    	}
    }
    */
}