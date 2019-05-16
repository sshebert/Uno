package Game.Shared;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
/**All AES encryption was written by Alex L. The two functions used in the rest of the project were written by Benjamin Groman.
 * @auther Alex L
 * @author Benjamin Groman
 *
 */
public class AESEncrytion {
    private byte[] key;
    private static final String ALGORITHM = "AES";

    public AESEncrytion(String key){
        this.key = key.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] encrypt(ArrayList hand){
        try {
        	byte[] uncryptData = getBytes(hand);
            SecretKeySpec secretKey = new SecretKeySpec(key,ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(uncryptData);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**A simple XOR of the given bytes with the key from the constructor
     * @author Benjamin Groman
     * @param data The bytes to encrypt
     * @return The encrypted bytes, or the original bytes if it failed to encrypt
     */
    public byte[] encrypt(byte[] data) {
    	try {
    		byte[] b = new byte[data.length];
    		for (int i = 0; i < data.length; i++) {
    			b[i] = (byte)(data[i] ^ key[i%key.length]);
    		}
    		return b;
            //SecretKeySpec secretKey = new SecretKeySpec(key,ALGORITHM);
            //Cipher cipher = Cipher.getInstance(ALGORITHM);
            //cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            //return cipher.doFinal(data);
    	}
    	catch (Exception e) {
    		return data;
    	}
    }
    /**Does the exact same thing as encrypt because XOR is its own inverse.
     * Name kept to avoid changing references in other files, and also in case AES is desired later.
     * @author Benjamin Groman
     * @param encryption The bytes to decrypt
     * @return The decrypted bytes, or the original bytes if decryption failed
     */
    public byte[] decryptToBytes(byte[] encryption) {
    	try {
    		return encrypt(encryption);
            //SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
            //Cipher cipher = Cipher.getInstance(ALGORITHM);
            //cipher.init(Cipher.DECRYPT_MODE, secretKey);
            //return cipher.doFinal(encryption);
        }
    	catch (Exception e) {
    		return encryption;
    	}
    }

    public ArrayList decrypt(byte[] encryption){
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] uncryptData = cipher.doFinal(encryption);
            return getHand(uncryptData);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] getBytes(ArrayList hand) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(hand);
        return baos.toByteArray();
    }

    private ArrayList<Integer> getHand(byte[] uncryptData) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(uncryptData);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (ArrayList<Integer>) ois.readObject();
    }

}
