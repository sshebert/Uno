//author Sam Shebert

package Game.Shared;


public enum CardType {
    Zero(0),
    One(1),
    Two(2),
    Three(3),
    Four(4),
    Five(5),
    Six(6),
    Seven(7),
    Eight(8),
    Nine(9),
    DrawTwo(10),
    Skip(11),
    Reverse(12),
    Wild(13),
    WildDraw(14);

    private final byte byteCode;

    private CardType(int byteCode){
        this.byteCode = (byte)byteCode;
    }

    public byte getByte(){
        return byteCode;
    }
}

