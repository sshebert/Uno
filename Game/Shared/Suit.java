//author Sam Shebert

package Game.Shared;

public enum Suit {
    Red(0),
    Green(1),
    Yellow(2),
    Blue(3),
    Wild(4);

    private final byte byteCode;

    private Suit(int byteCode){
        this.byteCode = (byte)byteCode;
    }

    public byte getByte(){
        return byteCode;
    }

    public static Suit fromByte(byte byteCode){
        for(Suit suit : values()){
            if (suit.byteCode == byteCode)
                return suit;
        }
        throw new IllegalArgumentException();
    }
}
