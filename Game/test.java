package Game;

import Game.Shared.AESEncrytion;

import java.util.ArrayList;

public class test {

    public static void main(String[] args){
        AESEncrytion aes = new AESEncrytion("ekydkg26b38sltg5");
        ArrayList<Integer> bark = new ArrayList<>();
        bark.add(1);bark.add(1);
        System.out.println(bark);
        byte[] crypt = aes.encrypt(bark);
        System.out.println(crypt);
        bark = (ArrayList<Integer>)aes.decrypt(crypt);
        System.out.println(bark);
    }
}
