/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game.Shared;

import Game.Client.ClientGame;

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

    /*
    for codes: 0, 2, 3, 4, 8, 9, 10, 12
     */
    public EnCode(int ackOpCode){
        try {
            stream.write(intToBytes(ackOpCode));
        }
        catch (IOException exp){
            exp.printStackTrace();
        }
    }

    /*
    for codes: 1
     */

    public EnCode(String ip){
        try {
            stream.write(intToBytes(1));
            stream.write(ip.getBytes());
        }
        catch (IOException exp){
            exp.printStackTrace();
        }
    }

    /*
    for codes: 5, 6, 7, 11
     */

    public EnCode(Card card, int opCode){
        try {
            stream.write(intToBytes(opCode));
            if (card != null) {
                ObjectOutputStream out = new ObjectOutputStream(stream);
                out.writeObject(card);
            }
        }
        catch (IOException exp){
            exp.printStackTrace();
        }
    }

    public EnCode(ClientGame game, int opCode){
        try {
            stream.write(intToBytes(opCode));
            if (game != null) {
                ObjectOutputStream out = new ObjectOutputStream(stream);
                out.writeObject(game);
            }
        }
        catch (IOException exp){
            exp.printStackTrace();
        }
    }

    public EnCode(Boolean skip, int opCode){
        try {
            stream.write(intToBytes(opCode));
            if(skip){
                stream.write(1);
            }else{
                stream.write(0);
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
        return stream.toByteArray();
    }

    public int getSize(){
        return stream.toByteArray().length;
    }

}
