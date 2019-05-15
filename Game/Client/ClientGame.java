//Author Sam Shebert

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
        for (int i = 0; i < startingHandSize; i++) {//deal cards
            first.drawCard(deck.drawCard());
            while (players.next() != first)
                players.current().drawCard(deck.drawCard());
        }
        nextTurn();
        deck.playFirstCard();
    }

    public boolean checkCurrPlayer() {
        try {
            if (currPlayer.getInetAddress().equals(InetAddress.getLocalHost())) {//check player based on IP address
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

    public Card[] getSkipCards() {//handles skip cards, removes from deck and assigns to player
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
        if(player.equals(currPlayer)){//calling uno for yourself
            if(currPlayer.getHandLength() <= 2){//must have 2 cards to call uno on yourself
                currPlayer.setUnoSafe(true);
                return true;
            }else{//if you have more than 2 cards then draw cards
                drawCard();
                drawCard();
                currPlayer.setUnoSafe(false);
                return false;
            }
        }else{//calling uno on someone else
            boolean b = false;
            for(int i = 0; i < players.size(); i++){//check that someone has 1 cards
                players.next();
                if(players.current().getHandLength() == 1){
                    players.current().drawCard(deck.drawCard());
                    players.current().drawCard(deck.drawCard());
                    b = true;
                }
            }
            if(!b){//if no one has 1 card, then you draw card
                players.seek(player);
                players.current().drawCard(deck.drawCard());
                players.current().drawCard(deck.drawCard());
                players.seek(currPlayer);
            }
            return b;
        }
    }

    public boolean resolveUno(InetAddress playerInet){//get player who called uno based on ip
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

    public boolean playDrawCard(PlayerMove playerMove) {//case if someone drew a card and is trying to play it
        if (playerMove.card.equals(currPlayer.getLastDrewCard())) {
            if (Card.checkMove(deck.getTopCard(), playerMove.card)) {
                resolveMove(playerMove);
                return true;
            }
        }
        return false;
    }

    public Card getCard(int index){//method to play card
        return currPlayer.getCard(index);
    }

    public Card drawCard(){
        Card drawCard = deck.drawCard();
        currPlayer.drawCard(drawCard);
        return drawCard;
    }

    private void resolveWild(Suit wildSuit) {//when wild is a played, another temporary card is put on top with the color of the wild
        Card blankWild = new Card(CardType.Wild, wildSuit);
        deck.addWild(blankWild);
    }

    private void resolveWildDraw(Suit wildSuit) {//same as wild but for drawing
        Card blankWild = new Card(CardType.WildDraw, wildSuit);
        deck.addWild(blankWild);
    }

    private void resolveMove(PlayerMove playerMove) {//meant for resolving special cards, sets internal logic for handling special cards
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
}
