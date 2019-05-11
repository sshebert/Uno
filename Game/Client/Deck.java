//author Sam Shebert

package Game.Client;

import Game.Shared.Card;
import Game.Shared.CardType;
import Game.Shared.Suit;

import java.util.Collections;
import java.util.LinkedList;

class Deck {
    private LinkedList<Card> drawPile;
    private LinkedList<Card> playedCardsPile;
    private Card blankWild = null;

    Deck(int multiple){
        //could add a for loop around addCards() to add multiple decks with a parameter controller how many multiples
        drawPile = new LinkedList<>();
        playedCardsPile = new LinkedList<>();
        for(int i = 0; i < multiple; i++)
            addCards();
    }

    private void addCards() {
        //kind of messy, need to separate into 2 cases, wild or color suit
        //then inside of the if/else put the right number of cards for that suit
        //1 zero, 2 of every other card per every color suit and then 4 wild and 4 wild draw for wild suit
        for (Suit s : Suit.values()) {
            for (CardType t : CardType.values()) {
                if(s != Suit.Wild){
                    switch (t) {
                        case Zero:
                            drawPile.addFirst(new Card(t,s));
                            break;
                        case Wild:
                            break;
                        case WildDraw:
                            break;
                        default:
                            drawPile.addFirst(new Card(t,s));
                            drawPile.addFirst(new Card(t,s));
                            break;
                    }
                }
                else{
                    switch (t){
                        case Wild:
                            drawPile.addFirst(new Card(t,s));
                            drawPile.addFirst(new Card(t,s));
                            drawPile.addFirst(new Card(t,s));
                            drawPile.addFirst(new Card(t,s));
                            break;
                        case WildDraw:
                            drawPile.addFirst(new Card(t,s));
                            drawPile.addFirst(new Card(t,s));
                            drawPile.addFirst(new Card(t,s));
                            drawPile.addFirst(new Card(t,s));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(drawPile);
    }

    Card getTopCard(){
        return playedCardsPile.getFirst();
    }

    Card drawCard(){
        //if no more cards to draw, do work
        //give first card
        if(drawPile.size() == 0){
            Card topCard = playedCardsPile.removeFirst();
            drawPile = playedCardsPile;
            playedCardsPile = new LinkedList<>();
            playedCardsPile.addFirst(topCard);
            shuffle();
        }
        return drawPile.removeFirst();
    }

    boolean playCard(Card newCard){
        if(Card.checkMove(playedCardsPile.getFirst(), newCard)){
            //wild card cleanup
            if(blankWild == playedCardsPile.getFirst()){
                //remove blank card
                playedCardsPile.removeFirst();
                blankWild = null; //book keeping
            }
            playedCardsPile.addFirst(newCard);
            return true;
        }
        else
            return false;
    }

    void playFirstCard(){
        Card temp = drawCard();
        //not completely correct but prevents an action card from being the first card on the pile
        while(temp.getCardVal() == CardType.WildDraw || temp.getCardVal() == CardType.Wild || temp.getCardVal() == CardType.DrawTwo || temp.getCardVal() == CardType.Skip){
            drawPile.addLast(temp);
            temp = drawCard();
        }
        playedCardsPile.addFirst(temp);
    }

    void addWild(Card blankWild){
        //remember blank card because it will need to be removed
        this.blankWild = blankWild;
        playedCardsPile.addFirst(blankWild);
    }
}
