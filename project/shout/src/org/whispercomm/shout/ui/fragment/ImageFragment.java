
package org.whispercomm.shout.ui.fragment;

import java.io.IOException;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.HashReference;
import org.whispercomm.shout.R;
import org.whispercomm.shout.ShoutImage;
import org.whispercomm.shout.content.ContentManager;
import org.whispercomm.shout.content.ShoutImageStorage;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;

public class ImageFragment extends SherlockFragment {
	private static final String TAG = ImageFragment.class.getSimpleName();

	private static final String HASH_KEY = "hash";

	public static final ImageFragment newInstance(Hash hash) {
		Bundle args = new Bundle();
		args.putByteArray(HASH_KEY, hash.toByteArray());

		ImageFragment f = new ImageFragment();
		f.setArguments(args);

		return f;
	}

	private ShoutImageStorage storage;

	private Hash hash;

	private boolean loaded;

	private ImageView viewImage;

	private View viewNotAvailable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.storage = new ShoutImageStorage(new ContentManager(this.getActivity()));
		this.hash = new Hash(getArguments().getByteArray(HASH_KEY));
		this.loaded = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_image, container, false);
		this.viewImage = (ImageView) view.findViewById(R.id.imgContent);
		this.viewNotAvailable = (View) view.findViewById(R.id.llNotAvailable);
		return view;
	}

	private void loadImage() {
		try {
			HashReference<ShoutImage> ref = storage.retrieve(hash);
			if (ref.isAvailable()) {
				viewImage.setImageBitmap(Bitmap.createScaledBitmap(ref.get().getBitmap(), 768,
						1024, true));
				viewImage.setVisibility(ImageView.VISIBLE);
				viewNotAvailable.setVisibility(View.GONE);
				loaded = true;
			}
		} catch (IOException e) {
			Log.w(TAG, "Unable to load Shout image.", e);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!loaded)
			loadImage();
	}

}
