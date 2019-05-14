/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game.Shared;

import Game.Client.ClientGame;
import Game.Client.Main;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

/**
 *
 * @author alawren3
 */
public class EnCode {

    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    //opcode 3
    public EnCode(int ackOpCode){
        try {
            stream.write(intToBytes(ackOpCode));
        }
        catch (IOException exp){
            exp.printStackTrace();
        }
    }

    //opcode 1
    public EnCode(String ip){
        try {
            stream.write(intToBytes(1));
            stream.write(ip.getBytes());
        }
        catch (IOException exp){
            exp.printStackTrace();
        }
    }

    //opcode 2
    public EnCode(ClientGame game){
        try {
            stream.write(intToBytes(2));
            if (game != null) {
                ObjectOutputStream out = new ObjectOutputStream(stream);
                out.writeObject(game);
            }
        }
        catch (IOException exp){
            exp.printStackTrace();
        }
    }

    //opcode 0
    public EnCode(Player player){
        try {
            stream.write(intToBytes(0));
            if (player != null) {
                ObjectOutputStream out = new ObjectOutputStream(stream);
                out.writeObject(player);
            }
        }
        catch (IOException exp){
            exp.printStackTrace();
        }
    }

    private byte[] intToBytes(int i){
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    public byte[] getHeader(){
        return new AESEncrytion(Main.getKey()).encrypt(stream.toByteArray());
    }

    public int getSize(){
        return stream.toByteArray().length;
    }

}