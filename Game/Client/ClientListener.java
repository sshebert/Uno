package Game.Client;

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