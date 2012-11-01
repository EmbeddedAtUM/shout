
package org.whispercomm.shout.ui.preference;

import org.whispercomm.android.preference.DelegatedPreference;
import org.whispercomm.shout.R;
import org.whispercomm.shout.util.ImageUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class BitmapPreference extends DelegatedPreference<Bitmap> {
	private static final String DEFAULT_COMPRESS_FORMAT = "PNG";
	private static final int DEFAULT_COMPRESS_QUALITY = 100;
	private static final int IMAGE_DIM_MAX = 73; // 73 pixels
	private static final int IMAGE_SIZE_MAX = 10 * 1024; // 10 KB

	private ImageView mImageView;
	private Drawable mDrawable;

	private int mMaxWidth;
	private int mMaxHeight;
	private CompressFormat mCompressFormat;
	private int mCompressQuality;
	private int mMaxSize;

	public BitmapPreference(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.bitmapPreferenceStyle);
	}

	public BitmapPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.BitmapPreference, defStyle, 0);
		extractXmlAttributes(a);
		a.recycle();

		this.registerDelegate(new CameraBitmapDelegate(getContext()));
		this.registerDelegate(new GalleryBitmapDelegate(getContext()));
		this.registerDelegate(new TwitterProfileImageDelegate(getContext()));
	}

	private void extractXmlAttributes(TypedArray a) {
		setMaxWidth(a.getInt(R.styleable.BitmapPreference_maxScaledWidth,
				IMAGE_DIM_MAX));
		setMaxHeight(mMaxHeight = a.getInt(R.styleable.BitmapPreference_maxScaledHeight,
				IMAGE_DIM_MAX));
		setMaxSize(a.getInt(R.styleable.BitmapPreference_maxCompressedSize,
				IMAGE_SIZE_MAX));
		setCompressQuality(a.getInt(
				R.styleable.BitmapPreference_compressionQuality,
				DEFAULT_COMPRESS_QUALITY));
		String compressFormat = a
				.getString(R.styleable.BitmapPreference_compressionFormat);
		setCompressFormat(CompressFormat.valueOf(compressFormat != null ? compressFormat
				: DEFAULT_COMPRESS_FORMAT));
	}

	/**
	 * Gets the maximum width to which newly persisted images will be scaled.
	 * 
	 * @return the maximum width to which images are scaled in pixels
	 */
	public int getMaxWidth() {
		return mMaxWidth;
	}

	/**
	 * Sets the width to which newly persisted images will be scaled.
	 * 
	 * @param maxWidth the width to which images are scaled in pixels
	 */
	public void setMaxWidth(int maxWidth) {
		this.mMaxWidth = maxWidth;
	}

	/**
	 * Gets the maximum height to which newly persisted images will be scaled.
	 * 
	 * @return the maximum height to which images are scaled in pixels
	 */
	public int getMaxHeight() {
		return mMaxHeight;
	}

	/**
	 * Sets the height to which newly persisted images will be scaled.
	 * 
	 * @param maxHeight the height to which images are scaled in pixels
	 */
	public void setMaxHeight(int maxHeight) {
		this.mMaxHeight = maxHeight;
	}

	/**
	 * Gets the compression format used for persisting images.
	 * 
	 * @return the compression format used for persisting images
	 */
	public CompressFormat getCompressFormat() {
		return mCompressFormat;
	}

	/**
	 * Sets the compression format used for persisting images.
	 * 
	 * @param compressFormat the compression format used for persisting images
	 */
	public void setCompressFormat(CompressFormat compressFormat) {
		this.mCompressFormat = compressFormat;
	}

	/**
	 * Gets the compression quality used when persisting images.
	 * 
	 * @return the compression used when persisting images.
	 */
	public int getCompressQuality() {
		return mCompressQuality;
	}

	/**
	 * Sets the compression quality used when persisting images. Must be in the
	 * range [0, 100].
	 * 
	 * @param compressQuality the compression quality used when persisting
	 *            images
	 */
	public void setCompressQuality(int compressQuality) {
		if (compressQuality < 0 || compressQuality > 100)
			throw new IllegalArgumentException(String.format(
					"Argument compressQuality must be between 0 and 100 inclusive. Got %d.",
					compressQuality));
		this.mCompressQuality = compressQuality;
	}

	/**
	 * Gets the maximum allowable size of a compressed image. A larger image
	 * will be rejected.
	 * 
	 * @return the maximum allowable size of a compressed image
	 */
	public int getMaxSize() {
		return mMaxSize;
	}

	/**
	 * Sets the maximum allowable size of a compressed image. A larger image
	 * will be rejected.
	 * 
	 * @param maxSize the maximum allowable size of a compressed image
	 */
	public void setMaxSize(int maxSize) {
		this.mMaxSize = maxSize;
	}

	/**
	 * Sets and stores the bitmap. The image will be resized and compressed
	 * according to the {@code BitmapPreference} configuration.
	 * 
	 * @param bitmap the image to store
	 * @throws ImageTooLargeException if the resized and compressed image
	 *             exceeded the maximum allowable size
	 */
	public void setBitmap(Bitmap bitmap) throws ImageTooLargeException {
		ScaledAndCompressedBitmap scaled = new ScaledAndCompressedBitmap(bitmap, mMaxWidth,
				mMaxHeight, mCompressFormat, mCompressQuality);
		if (scaled.getCompressed().length > mMaxSize)
			throw new ImageTooLargeException(scaled.getCompressed().length, mMaxSize);

		if (callChangeListener(scaled.getResized()))
			onSaveBitmap(scaled);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getDrawable(index);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		if (!restoreValue)
			setImageDrawable((Drawable) defaultValue);

		Bitmap persisted = getPersistedBitmap();
		if (persisted != null)
			setImageDrawable(new BitmapDrawable(persisted));
		else
			setImageDrawable((Drawable) defaultValue);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		View imageView = view.findViewById(R.id.imageView);
		if (imageView != null && imageView instanceof ImageView) {
			mImageView = (ImageView) imageView;
			refreshImageView();
		}
	}

	public void onResult(Bitmap bitmap) {
		try {
			setBitmap(bitmap);
		} catch (ImageTooLargeException e) {
			Toast.makeText(getContext(), R.string.prefavatar_imagetoolarge, Toast.LENGTH_LONG)
					.show();
		}
	}

	private void onSaveBitmap(ScaledAndCompressedBitmap bitmap) throws ImageTooLargeException {
		setImageDrawable(new BitmapDrawable(bitmap.getResized()));
		String encoded = Base64.encodeToString(bitmap.getCompressed(), DEFAULT_ORDER);
		persistString(encoded);
		notifyChanged();
	}

	private void setImageDrawable(Drawable drawable) {
		mDrawable = drawable;
		refreshImageView();
	}

	private void refreshImageView() {
		if (mImageView != null)
			mImageView.setImageDrawable(mDrawable);
	}

	private Bitmap getPersistedBitmap() {
		String encoded = getPersistedString(null);
		if (encoded == null)
			return null;

		byte[] compressed = Base64.decode(encoded, DEFAULT_ORDER);
		Bitmap bitmap = BitmapFactory.decodeByteArray(compressed, 0, compressed.length);

		return bitmap;
	}

	/**
	 * Resized and compressed {@link Bitmap}.
	 * 
	 * @author David R. Bild
	 */
	public static class ScaledAndCompressedBitmap {

		private final Bitmap mResized;
		private final byte[] mCompressed;

		/**
		 * Constructs a new {@code ScaledAndCompressedImage}, resized to to fit
		 * within the specified dimensions, preserving aspect ratio, and
		 * compressed according to the specified format and quality.
		 * 
		 * @see {@link CompressFormat}
		 * @see {@link Bitmap#compress}
		 * @param bitmap the bitmap to resize and compress
		 * @param maxWidth the maximum width of the resized bitmap
		 * @param maxHeight the maximum height of the resized bitmap
		 * @param format the compression format
		 * @param quality the compression quality in range [0-100]
		 * @throws ImageTooLargeException if the compressed image exceeds
		 *             {@code maxCompressedSize}
		 */
		public ScaledAndCompressedBitmap(Bitmap bitmap, int maxWidth, int maxHeight,
				CompressFormat format,
				int quality) throws ImageTooLargeException {
			mResized = scaleIfNeeded(bitmap, maxWidth, maxHeight);
			mCompressed = ImageUtils.compressBitmap(mResized, format, quality);
		}

		/**
		 * Gets the resized {@link Bitmap}.
		 * 
		 * @return the resized bitmap
		 */
		public Bitmap getResized() {
			return mResized;
		}

		/**
		 * Gets the compressed from of the {@link Bitmap}.
		 * 
		 * @return the compressed bitmap
		 */
		public byte[] getCompressed() {
			return mCompressed;
		}

		/**
		 * Scales a bitmap to fix within the width and height. If no scaling is
		 * needed, the original bitmap is returned.
		 * 
		 * @param bitmap the bitmap to scale
		 * @param maxWidth the maximum width of the scaled bitmap
		 * @param maxHeight the maximum height of the scaled bitmap
		 * @return the scaled bitmap
		 */
		private static Bitmap scaleIfNeeded(Bitmap bitmap, int maxWidth, int maxHeight) {
			if (bitmap.getWidth() <= maxWidth && bitmap.getHeight() < maxHeight)
				return bitmap; // No scaling needed

			/* Scale to fit */
			RectF src = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
			RectF dst = new RectF(0, 0, maxWidth, maxHeight);

			Matrix m = new Matrix();
			m.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
			return Bitmap
					.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
		}

	}

	/**
	 * Thrown when an image that after scaling and compression exceeds the size
	 * limit specified for this preference instance is received.
	 * 
	 * @author David R. Bild
	 */
	public static class ImageTooLargeException extends RuntimeException {

		private static final long serialVersionUID = -4187077304520468366L;

		public ImageTooLargeException(long size, long maxSize) {
			super(String.format("Image size must be less than %d bytes. Got %d bytes.", size,
					maxSize));
		}

		public ImageTooLargeException(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}

		public ImageTooLargeException(String detailMessage) {
			super(detailMessage);
		}

		public ImageTooLargeException(Throwable throwable) {
			super(throwable);
		}

	}

}
