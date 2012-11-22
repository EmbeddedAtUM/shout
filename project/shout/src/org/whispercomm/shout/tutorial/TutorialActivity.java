
package org.whispercomm.shout.tutorial;

import org.whispercomm.shout.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

/**
 * Tutorial activity that uses a {@link ViewPager} to display a series of
 * instructional images.
 */
public class TutorialActivity extends FragmentActivity {

	private ViewPager mViewPager;
	private ViewPagerAdapter mViewPagerAdapter;
	private CirclePageIndicator mPageIndicator;

	public static void show(Context context)
	{
		Intent intent = new Intent(context, TutorialActivity.class);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tutorial);

		mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mViewPagerAdapter);

		mPageIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		mPageIndicator.setViewPager(mViewPager);
	}

	public void onClick()
	{
		finish();
	}

	/**
	 * Adapter to create the {@link Fragment} for each page. This class extends
	 * {@link FragmentStatePagerAdapter} so that the created fragments are
	 * cached in the background, but can be destroyed when not visible if need
	 * be.
	 * 
	 * @author David R. Bild
	 */
	private class ViewPagerAdapter extends FragmentStatePagerAdapter {

		private static final int NUM_PAGES = 9;

		public ViewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					return TutorialPageFragment.newInstance(R.drawable.image1,
							R.string.tutorial_page_1_message);
				case 1:
					return TutorialPageFragment.newInstance(R.drawable.image2,
							R.string.tutorial_page_2_message);
				case 2:
					return TutorialPageFragment.newInstance(R.drawable.image3,
							R.string.tutorial_page_3_message);
				case 3:
					return TutorialPageFragment.newInstance(R.drawable.image4,
							R.string.tutorial_page_4_message);
				case 4:
					return TutorialPageFragment.newInstance(R.drawable.image5,
							R.string.tutorial_page_5_message);
				case 5:
					return TutorialPageFragment.newInstance(R.drawable.image6,
							R.string.tutorial_page_6_message);
				case 6:
					return TutorialPageFragment.newInstance(R.drawable.image7,
							R.string.tutorial_page_7_message);
				case 7:
					return TutorialPageFragment.newInstance(R.drawable.image8,
							R.string.tutorial_page_8_message);
				case 8:
					return TutorialPageFragment.newInstance(R.drawable.image9,
							R.string.tutorial_page_9_message, true);
				default:
					throw new IllegalArgumentException(String.format(
							"position must be between %d and %d. Got %d.", 0, NUM_PAGES,
							position));
			}
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}
	}

	/**
	 * Fragment that contains a tutorial page. The page is optionally clickable.
	 * If enabled, {@link TutorialActivity#onClick()} will be called when
	 * clicked.
	 * 
	 * @author David R. Bild
	 */
	public static class TutorialPageFragment extends Fragment {

		/**
		 * Creates a new TutorialPageFragement to display the specified content.
		 * The fragment is not clickable.
		 * 
		 * @param backgroundResId the id of the background drawable
		 * @param textResId the id of the text to display over the image
		 * @return the newly created fragment
		 */
		public static TutorialPageFragment newInstance(int backgroundResId, int textResId) {
			return newInstance(backgroundResId, textResId, false);
		}

		/**
		 * Creates a new TutorialPageFragment to display the specified content.
		 * If {@code clickable} is {@code true},
		 * {@link TutorialActivity#onClick()} will be called when clicked.
		 * 
		 * @param backgroundResId the id of the background drawable
		 * @param textResId the id of the text to display over the image
		 * @param clickable if the page should be clickable
		 * @return the newly created fragment
		 */
		public static TutorialPageFragment newInstance(int backgroundResId, int textResId,
				boolean clickable) {
			TutorialPageFragment f = new TutorialPageFragment();

			Bundle args = new Bundle();
			args.putInt(BACKGROUND_RESID_KEY, backgroundResId);
			args.putInt(TEXT_RESID_KEY, textResId);
			args.putBoolean(CLICKABLE_KEY, clickable);

			f.setArguments(args);
			return f;
		}

		private static final String BACKGROUND_RESID_KEY = "background_resid_key";
		private static final String TEXT_RESID_KEY = "text_resid_key";
		private static final String CLICKABLE_KEY = "clickable_key";

		private int mBackgroundResId;
		private int mTextResId;
		private boolean mClickable;

		private ViewGroup mRoot;
		private TextView mTextView;

		private OnClickListener mOnClickListener;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mBackgroundResId = getArguments().getInt(BACKGROUND_RESID_KEY);
			mTextResId = getArguments().getInt(TEXT_RESID_KEY);
			mClickable = getArguments().getBoolean(CLICKABLE_KEY);

			if (mClickable) {
				mOnClickListener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						((TutorialActivity) getActivity()).onClick();
					}
				};
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			mRoot = (ViewGroup) inflater.inflate(R.layout.tutorial_page_fragment, null);
			initTextView();
			return mRoot;
		}

		private void initTextView() {
			mTextView = (TextView) mRoot.findViewById(R.id.content);
			mTextView.setBackgroundResource(mBackgroundResId);
			mTextView.setText(mTextResId);
			if (mOnClickListener != null)
				mTextView.setOnClickListener(mOnClickListener);
		}

	}

}
