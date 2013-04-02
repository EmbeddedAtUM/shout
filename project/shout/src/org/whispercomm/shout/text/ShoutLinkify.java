
package org.whispercomm.shout.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.widget.TextView;

public class ShoutLinkify {

	public static final Pattern SHOUT_URI = Pattern.compile("shout://\\p{XDigit}{64}",
			+Pattern.CASE_INSENSITIVE);

	public static final String LINK_TEXT = "Image";

	public static final boolean addLinks(TextView text) {
		CharSequence t = text.getText();

		SpannableStringBuilder s = SpannableStringBuilder.valueOf(t);

		if (addLinks(s)) {
			addLinkMovementMethod(text);
			text.setText(s);

			return true;
		}

		return false;
	}

	private static final void addLinkMovementMethod(TextView t) {
		MovementMethod m = t.getMovementMethod();

		if ((m == null) || !(m instanceof LinkMovementMethod)) {
			if (t.getLinksClickable()) {
				t.setMovementMethod(LinkMovementMethod.getInstance());
			}
		}
	}

	private static final boolean addLinks(SpannableStringBuilder s) {
		boolean match = false;

		Matcher matcher = SHOUT_URI.matcher(s);
		while (matcher.find()) {
			match = true;

			int start = matcher.start();
			int end = matcher.end();

			String uri = matcher.group();

			SpannableString replacement = new SpannableString(LINK_TEXT);
			replacement.setSpan(new ShoutUriSpan(uri), 0, LINK_TEXT.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			s.replace(start, end, replacement);

			matcher = SHOUT_URI.matcher(s);
		}

		return match;
	}
}
