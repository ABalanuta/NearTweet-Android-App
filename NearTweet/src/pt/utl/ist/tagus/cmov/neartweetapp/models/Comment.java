package pt.utl.ist.tagus.cmov.neartweetapp.models;

import android.graphics.Bitmap;

public class Comment{
	
    public String username;
    public String comment;
    public Bitmap avatar;
        
    public Comment(String username, String comment) {
        this.username = username;
        this.comment = comment;
    }
    public Comment(String username, String comment, Bitmap avatar) {
    	this.username = username;
        this.comment = comment;
        this.avatar = avatar;
    }
}