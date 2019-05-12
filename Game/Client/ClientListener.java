package Game.Client;

import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import Game.Shared.Constants;
import Game.Shared.CyclicLinkedList;

/**
 * Handles the gathering and basic filtering of user input.
 * @author Benjamin Groman
 *
 */
class ClientListener implements Runnable {
    ConcurrentLinkedQueue queue;

	/*for debugging
	 *testers should also uncomment the Main class at the bottom of the file 
	public static void main(String[] args) {
		new ClientListener().run();
	}
	*/
	ClientListener(ConcurrentLinkedQueue q){
	    queue=q;
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
                    queue.add(Constants.exit);
                    exitRequested = true;
                }
                //for drawing a card
                else if (message.equalsIgnoreCase("draw")) {
                    queue.add(Constants.drawCard);
                }
                //for declaring the color after playing a wild
                else if (message.equalsIgnoreCase("blue")) {
                    queue.add(Constants.blue);
                } else if (message.equalsIgnoreCase("red")) {
                    queue.add(Constants.red);
                } else if (message.equalsIgnoreCase("green")) {
                    queue.add(Constants.green);
                } else if (message.equalsIgnoreCase("yellow")) {
                    queue.add(Constants.yellow);
                }
                //for selecting whether to attempt to play the card that was just picked up
                else if (message.equalsIgnoreCase("play")) {
                    queue.add(Constants.play);
                } else if (message.equalsIgnoreCase("hold")) {
                    queue.add(Constants.hold);
                }
                //for starting the game
                else if (message.equalsIgnoreCase("start") || message.equalsIgnoreCase("start game")) {
                    queue.add(Constants.startGame);
                }
                //positive integers for selecting a card to play
                //cap the size at three digits to prevent overflows in parsing
                //having a 100 cards is possible if two decks are combined, but rare
                //second check screens values below the minimum index, which could be negative for all this class cares
                else if (message.matches("-?\\d{1,3}") && Integer.parseInt(message) >= Constants.minIndex) {
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
    	System.out.println("You have " + (Constants.timeoutNanos / 1000000000) + " seconds for your turn.");
    	System.out.println("Commands:");
    	System.out.println("start, start game - start the game if you are the host");
    	System.out.println("help - print this message");
    	System.out.println("exit - quit the game");
    	System.out.println("draw - draw a card");
    	System.out.println("play - play the card that was just drawn");
    	System.out.println("hold - don't play the card that was just drawn");
    	System.out.println("red, blue, green, yellow - select a color after playing a wild card");
    	String indices = "";
    	for (int i = Constants.minIndex; i < Constants.minIndex + 3; i++) {
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