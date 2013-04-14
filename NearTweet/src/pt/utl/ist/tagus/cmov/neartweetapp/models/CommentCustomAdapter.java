package pt.utl.ist.tagus.cmov.neartweetapp.models;

import java.util.ArrayList;

import pt.utl.ist.tagus.cmov.neartweet.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CommentCustomAdapter extends ArrayAdapter<Comment> {

	private ArrayList<Comment> comments;

	public CommentCustomAdapter(Context context, int textViewResourceId, ArrayList<Comment> comments) {
		super(context, textViewResourceId, comments);
		this.comments = comments;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.listitem, null);
		}

		Comment comment = comments.get(position);
		if (comment != null) {
			TextView mUsername = (TextView) v.findViewById(R.id.username);
			TextView mComment = (TextView) v.findViewById(R.id.comment);

			if (mUsername != null) {
				mUsername.setText(comment.username);
			}

			if(mComment != null) {
				mComment.setText("Comment: " + comment.comment);
			}
		}
		return v;
	}
}