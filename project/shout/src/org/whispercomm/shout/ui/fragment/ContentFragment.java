
package org.whispercomm.shout.ui.fragment;

import org.whispercomm.shout.Hash;
import org.whispercomm.shout.MimeType;
import org.whispercomm.shout.R;
import org.whispercomm.shout.content.ContentManager;
import org.whispercomm.shout.content.descriptor.ContentDescriptorReference;
import org.whispercomm.shout.content.descriptor.ContentDescriptorStore;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class ContentFragment extends SherlockFragment {
	private static final String TAG = ContentFragment.class.getSimpleName();

	private static final String HASH_KEY = "hash";

	public static final ContentFragment newInstance(Hash hash) {
		Bundle args = new Bundle();
		args.putByteArray(HASH_KEY, hash.toByteArray());

		ContentFragment f = new ContentFragment();
		f.setArguments(args);

		return f;
	}

	private ContentListener activity;

	private ContentDescriptorStore storage;

	private Hash hash;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (ContentListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.storage = new ContentManager(this.getActivity()).getDescriptorStore();
		this.hash = new Hash(getArguments().getByteArray(HASH_KEY));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_content, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		loadContent();
	}

	private void loadContent() {
		ContentDescriptorReference ref = storage.retrieve(hash);
		if (ref.isAvailable()) {
			dispatch(ref.get().getMimeType());
		}
	}

	private void dispatch(MimeType mimeType) {
		final SherlockFragment f;

		if (mimeType.is(MimeType.GIF, MimeType.JPEG, MimeType.PNG)) {
			f = ImageFragment.newInstance(hash);
		} else {
			Log.w(TAG, String.format("Unknown content type: %s.", mimeType.toString()));
			return;
		}

		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				activity.displayContent(f);
			}
		});
	}

	/**
	 * Interface that Activities hosting a ContentFragment must implement. The
	 * callback defined here is used to notify the activity when the content
	 * MIME type is available and the appropriate Fragment for viewing that
	 * content can replace this ContentFragment.
	 */
	public interface ContentListener {

		/**
		 * Called once the MIME type of the content is known and the appropriate
		 * Fragment for display can be determined.
		 * 
		 * @param fragment the fragment that will display the content
		 */
		public void displayContent(SherlockFragment fragment);

	}

}
