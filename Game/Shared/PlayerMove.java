package Game.Shared;

public class PlayerMove {
    public Card card;//card player wants to play, can be null which would signal player wants to draw a card
    public Suit wildSuit;//if player is playing a wild card they need to include which suit they are changing too, else this will be null
    public boolean draw;

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
