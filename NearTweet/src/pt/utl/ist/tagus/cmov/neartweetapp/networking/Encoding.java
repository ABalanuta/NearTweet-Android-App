package pt.utl.ist.tagus.cmov.neartweetapp.networking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import pt.utl.ist.tagus.cmov.neartweetapp.models.Tweet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Encoding {

	
	static public byte[] encodeImage(Bitmap bitmap){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		return stream.toByteArray();
	}

	static public  Bitmap decodeImage(byte[] b){
		InputStream is = new ByteArrayInputStream(b);
		return BitmapFactory.decodeStream(is);
	}
	
	static public byte[] encodeTweet(Tweet tweet){
		
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
	        ObjectOutputStream o;
			o = new ObjectOutputStream(b);
			o.writeObject(tweet);
	        return b.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	static public Tweet decodeTweet(byte[] bytes){
		
		try {
			ByteArrayInputStream b = new ByteArrayInputStream(bytes);
	        ObjectInputStream o;
			o = new ObjectInputStream(b);
			return (Tweet) o.readObject();
			
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
        return null;
	}
	
	
}
