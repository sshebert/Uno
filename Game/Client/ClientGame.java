package Game.Client;

import Game.Shared.*;

import java.io.Serializable;
import java.net.InetAddress;

public class ClientGame implements Serializable{
    private Deck deck;
    private boolean drawTwo = false;
    private boolean drawFour = false;
    private boolean skip = false;
    private Player currPlayer;
    private Player winner;
    private int startingHandSize = 7;
    private Card currCard;
    private CyclicLinkedList<Player> players;
    private boolean gameRunning = true;

    public ClientGame(CyclicLinkedList<Player> players, int multiple) {
        this.players = players;
        deck = new Deck(multiple);

        Player first = currPlayer = players.current();
        for (int i = 0; i < startingHandSize; i++) {
            first.drawCard(deck.drawCard());
            while (players.next() != first)
                players.current().drawCard(deck.drawCard());
        }
        nextTurn();
        deck.playFirstCard();
    }

    public boolean checkCurrPlayer() {
        try {
            if (currPlayer.getInetAddress().equals(InetAddress.getLocalHost())) {
                return true;
            } else {
                return false;
            }
        }catch (Exception exp){
            exp.printStackTrace();
        }
        return false;
    }

    //these methods should more or less be executed in series by the server
    //increment to next players turn and reset skip
    public Player nextTurn() {
        currPlayer = players.next();
        return currPlayer;
    }

    public void removePlayer(Player p){
        players.remove(p);
    }

    public boolean checkSkip() {
        if(skip){
            skip = false;
            return true;
        }
        return skip;
    }

    public Card[] getSkipCards() {
        Card[] cards = null;
        if (drawTwo) {
            cards = new Card[2];
            cards[0] = deck.drawCard();
            cards[1] = deck.drawCard();
            currPlayer.drawCard(cards[0]);
            currPlayer.drawCard(cards[1]);
            drawTwo = false;
        } else if (drawFour) {
            cards = new Card[4];
            cards[0] = deck.drawCard();
            cards[1] = deck.drawCard();
            cards[2] = deck.drawCard();
            cards[3] = deck.drawCard();
            currPlayer.drawCard(cards[0]);
            currPlayer.drawCard(cards[1]);
            currPlayer.drawCard(cards[2]);
            currPlayer.drawCard(cards[3]);
            drawFour = false;
        }
        return cards;
    }

    public boolean callUno(Player player){
        if(player.equals(currPlayer)){
            if(currPlayer.getHandLength() <= 2){
                currPlayer.setUnoSafe(true);
                return true;
            }else{
                drawCard();
                drawCard();
                currPlayer.setUnoSafe(false);
                return false;
            }
        }else{
            boolean b = false;
            for(int i = 0; i < players.size(); i++){
                players.next();
                if(players.current().getHandLength() == 1){
                    players.current().drawCard(deck.drawCard());
                    players.current().drawCard(deck.drawCard());
                    b = true;
                }
            }
            if(!b){
                players.seek(player);
                players.current().drawCard(deck.drawCard());
                players.current().drawCard(deck.drawCard());
                players.seek(currPlayer);
            }
            return b;
        }
    }

    public boolean resolveUno(InetAddress playerInet){
        Player out = null;
        for(int i = 0; i < players.size(); i++){
            players.next();
            if(players.current().getInetAddress().equals(playerInet)){
               out = players.current();
            }
        }
        return callUno(out);
    }

    //if player move card is null, then draw card
    public boolean playCard(PlayerMove playerMove) {
            if (Card.checkMove(deck.getTopCard(), playerMove.card)) {
                //case for successfully played card
                resolveMove(playerMove);
                if(currPlayer.getHandLength() == 0){
                    gameRunning = false;
                    winner = currPlayer;
                }
                return true;
            }
        //case for card not in hand or not valid move
        return false;
    }

    public boolean playDrawCard(PlayerMove playerMove) {
        if (playerMove.card.equals(currPlayer.getLastDrewCard())) {
            if (Card.checkMove(deck.getTopCard(), playerMove.card)) {
                resolveMove(playerMove);
                return true;
            }
        }
        return false;
    }

    public Card getCard(int index){
        return currPlayer.getCard(index);
    }

    public Card drawCard(){
        Card drawCard = deck.drawCard();
        currPlayer.drawCard(drawCard);
        return drawCard;
    }

    private void resolveWild(Suit wildSuit) {
        Card blankWild = new Card(CardType.Wild, wildSuit);
        deck.addWild(blankWild);
    }

    private void resolveWildDraw(Suit wildSuit) {
        Card blankWild = new Card(CardType.WildDraw, wildSuit);
        deck.addWild(blankWild);
    }

    private void resolveMove(PlayerMove playerMove) {
        deck.playCard(playerMove.card);
        currPlayer.playCard(playerMove.card);
        //handle action cards
        switch (playerMove.card.getCardVal()) {
            case Wild:
                resolveWild(playerMove.wildSuit);
                break;
            case WildDraw:
                resolveWildDraw(playerMove.wildSuit);
                skip = true;
                drawFour = true;
                break;
            case Reverse:
                players.flipDirection();
                break;
            case DrawTwo:
                drawTwo = true;
                skip = true;
                break;
            case Skip:
                skip = true;
                break;
            default:
                break;
        }
    }

    public boolean checkGameRunning(){
        return gameRunning;
    }

    public Player getCurrPlayer(){
        return currPlayer;
    }

    public Card getTopCard(){
        return deck.getTopCard();
    }

    public Player getWinner(){
        return winner;
    }


   /* public void run() {
        Scanner sc = new Scanner(System.in);
        //deal cards


        while (true) {
            Player p = players.next();
            //skip will always be true if drawTwo or drawFour is true, so only checking skip for now
            if (!skip) {
                System.out.println("\n" + p.getName() + "'s turn");
                System.out.println("Top card is " + deck.getTopCard().toString() + "\n");
                System.out.println("Your cards are:");
                p.printCards();

                boolean moveValid = false;
                while (!moveValid) {
                    System.out.println("Enter the index of the card you wish to play or type draw");
                    String move = sc.nextLine();

                    if (move.equalsIgnoreCase("draw")) {
                        Card drawCard = deck.drawCard();
                        p.drawCard(drawCard);
                        System.out.println(drawCard.toString() + " drawn");
                        System.out.println("Type yes to play card draw");
                        move = sc.nextLine();
                        if (move.equalsIgnoreCase("yes")) {
                            if (ClientGame.checkMove(deck.getTopCard(), drawCard)) {
                                p.playCard(drawCard);
                                deck.playCard(drawCard);
                            }
                        }
                        moveValid = true;
                    } else if (isNumeric(move)) {
                        int cardNum = Integer.parseInt(move);
                        Card playCard = p.getCard(cardNum);

                        if (playCard.getSuit() == Suit.Wild) {
                            p.playCard(playCard);//remove from player hand
                            deck.playCard(playCard);//add to deck
                            //create temporary card based on desired suit and add to deck using special deck.addWild() method
                            //the deck will handle removing the blank card when the next card is played.
                            Card blankWild = null;
                            while (blankWild == null) {
                                System.out.println("What color would you like to change to?");
                                String newColor = sc.nextLine();
                                switch (newColor) {
                                    case "green":
                                        blankWild = new Card(CardType.Wild, Suit.Green);
                                        break;
                                    case "red":
                                        blankWild = new Card(CardType.Wild, Suit.Red);
                                        break;
                                    case "blue":
                                        blankWild = new Card(CardType.Wild, Suit.Blue);
                                        break;
                                    case "yellow":
                                        blankWild = new Card(CardType.Wild, Suit.Yellow);
                                        break;
                                    default:
                                        System.out.println("Invalid suit, pick green, red, blue, or yellow");
                                        break;
                                }
                            }
                            deck.addWild(blankWild);

                            if (playCard.getCardVal() == CardType.WildDraw) {
                                skip = true;
                                drawFour = true;
                            }
                            moveValid = true;

                        } else if (ClientGame.checkMove(deck.getTopCard(), playCard)) {
                            p.playCard(playCard);
                            deck.playCard(playCard);
                            switch (playCard.getCardVal()) {
                                case Reverse:
                                    players.flipDirection();
                                    break;
                                case DrawTwo:
                                    drawTwo = true;
                                    skip = true;
                                    break;
                                case Skip:
                                    skip = true;
                                    break;
                                default:
                                    break;
                            }
                            moveValid = true;
                        } else {
                            System.out.println("Move not valid, try again.");
                        }

                    } else if (move.equalsIgnoreCase("end")) {
                        moveValid = true;
                    } else {
                        System.out.println("Command not valid, try again.");
                    }

                    if (p.getHandLength() == 0) {
                        System.out.println(p.getName() + " won");
                        return;
                    }
                }
                //handle skip and stuff
            } else {
                System.out.println("\n" + p.getName() + "'s turn was skipped");
                if (drawTwo) {
                    Card temp = deck.drawCard();
                    System.out.println(p.getName() + " drew " + temp.toString());
                    p.drawCard(temp);

                    temp = deck.drawCard();
                    System.out.println(p.getName() + " drew " + temp.toString());
                    p.drawCard(temp);
                    drawTwo = false;
                } else if (drawFour) {
                    Card temp = deck.drawCard();
                    System.out.println(p.getName() + " drew " + temp.toString());
                    p.drawCard(temp);

                    temp = deck.drawCard();
                    System.out.println(p.getName() + " drew " + temp.toString());
                    p.drawCard(temp);

                    temp = deck.drawCard();
                    System.out.println(p.getName() + " drew " + temp.toString());
                    p.drawCard(temp);

                    temp = deck.drawCard();
                    System.out.println(p.getName() + " drew " + temp.toString());
                    p.drawCard(temp);
                    drawFour = false;
                }
                skip = false;
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of players");
        int playersNum = sc.nextInt();
        sc.nextLine();
        Player p = new Player("tempRemove");
        CyclicLinkedList<Player> tempPlayers = new CyclicLinkedList<Player>(p);
        for (int i = 1; i <= playersNum; i++) {
            System.out.println("Enter player " + i + "'s name");
            tempPlayers.add(new Player(sc.nextLine()));
        }
        tempPlayers.remove(p);
        ClientGame game = new ClientGame(tempPlayers, 1);
        game.run();
    }*/

}
