//author Sam Shebert

package Game.Shared;

import java.io.Serializable;

public class Card implements Serializable {
    private CardType value;
    private Suit suit;

    public Card(CardType value, Suit suit) {
        this.value = value;
        this.suit = suit;
    }

    public Suit getSuit(){
        return suit;
    }

    public CardType getCardVal(){
        return value;
    }

    public void setSuit(Suit s){
        suit = s;
    }

    public static boolean checkMove(Card topCard, Card newCard) {
        //if wild card then move is always valid
        //if the new cards suit or value matches the top cards then move is valid
        //else move is false
        if (newCard.getCardVal() == CardType.Wild || newCard.getCardVal() == CardType.WildDraw)
            return true;
        else if (newCard.getCardVal() == topCard.getCardVal() || newCard.getSuit() == topCard.getSuit())
            return true;
        else
            return false;
    }

    @Override
    public String toString(){
        return suit.toString() + " " + value.toString();
    }
}
