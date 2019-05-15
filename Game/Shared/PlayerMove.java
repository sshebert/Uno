//author Sam Shebert
//strictly used for communicating between UI(in Main) and ClientGame
//simplifies communication by putting all possible user decisions into one object and passing it to ClientGame class to resolve the players move.

package Game.Shared;

public class PlayerMove {
    public Card card;//card player wants to play
    public Suit wildSuit;//if player is playing a wild card they need to include which suit they are changing too, else this will be null
    public boolean draw;//signals player wants to draw card

    public PlayerMove(Card card) {
        this.card = card;
        wildSuit = null;
        draw = false;
    }

    public PlayerMove(Suit wildSuit) {
        this.card = null;
        this.wildSuit = wildSuit;
        draw = false;
    }

    public PlayerMove(Card card, Suit wildSuit) {
        this.card = card;
        this.wildSuit = wildSuit;
        draw = false;
    }

    public PlayerMove(boolean draw) {
        this.draw = draw;
        card = null;
        wildSuit = null;
    }

    public PlayerMove() {
        this.draw = false;
        card = null;
        wildSuit = null;
    }
}
