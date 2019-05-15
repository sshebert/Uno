//author Sam Shebert

package Game.Shared;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

public class Player implements Serializable {
    private static Constants C = new Constants();
    private ArrayList<Card> hand;
    private Card lastDrewCard;
    private String name;
    private InetAddress inetAddress;
    private boolean kill = false;//TTL for fault tolerance
    private boolean unoSafe = false;//uno check condition

    public Player(String name, InetAddress inetAddress) {
        hand = new ArrayList<>();
        this.name = name;
        this.inetAddress = inetAddress;
    }

    public void setKill(boolean b){
        kill = b;
    }

    public boolean getKill(){
        return kill;
    }

    public void setUnoSafe(boolean b){
        unoSafe = b;
    }

    public boolean getUnoSafe(){
        return unoSafe;
    }

    public boolean playCard(Card playCard) {
        //returns true and removes card from hand if card is in hand, else returns false
        return hand.remove(playCard);
    }

    public Card getCard(int index) {//play card with an index
        index -= C.minIndex;
        return hand.get(index);
    }

    public void drawCard(Card drawCard) {
        hand.add(drawCard);
        lastDrewCard = drawCard;
    }

    public Card getLastDrewCard() {
        return lastDrewCard;
    }

    public void printCards() {
        int count = C.minIndex;
        for (Card c : hand) {
            System.out.println(count + ") " + c.toString());
            count++;
        }
    }

    public int getHandLength() {
        return hand.size();
    }

    public ArrayList<Card> getHand(){
        return hand;
    }

    public String getName() {
        return name;
    }

    public boolean handContainsCard(Card card){
        return hand.contains(card);
    }

    public void updateHand(ArrayList<Card> hand){
        this.hand = hand;
    }

    public InetAddress getInetAddress(){
        return inetAddress;
    }
}
