
package org.whispercomm.shout.tracker;

import org.whispercomm.shout.Shout;
import org.whispercomm.shout.content.descriptor.ContentDescriptor;
import org.whispercomm.shout.content.merkle.MerkleNode;
import org.whispercomm.shout.content.request.ContentRequest;

import android.app.Application;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;

public class ShoutTracker {

	static final String CATEGORY_NETWORK_EVENT = "NetworkEvent";
	static final String CATEGORY_NETWORK_RECEIVE_EVENT = "NetworkReceiveEvent";
	static final String CATEGORY_NETOWRK_CREATE_EVENT = "NetworkCreateEvent";
	static final String CATEGORY_NETWORK_SEND_EVENT = "NetworkSendEvent";
	static final String CATEGORY_UI_EVENT = "UIEvent";

	static final String ACTION_RECEIVE_PACKET = "ReceivePacket";
	static final String ACTION_RECEIVE_CONTENT_DESCRIPTOR = "ReceiveContentDescriptor";
	static final String ACTION_RECEIVE_CONTENT_REQUEST = "ReceiveContentRequest";
	static final String ACTION_RECEIVE_MERKLE_NODE = "ReceiveMerkleNode";

	static final String ACTION_SEND_PACKET = "SendPacket";
	static final String ACTION_SEND_CONTENT_DESCRIPTOR = "SendContentDescriptor";
	static final String ACTION_SEND_CONTENT_REQUEST = "SendContentRequest";
	static final String ACTION_SEND_MERKLE_NODE = "SendMerkleNode";

	static final String ACTION_RECEIVE_SHOUT = "ReceiveShout";
	static final String ACTION_RECEIVE_RESHOUT = "ReceiveReshout";
	static final String ACTION_RECEIVE_COMMENT = "ReceiveComment";
	static final String ACTION_RECEIVE_RECOMMENT = "ReceiveRecomment";

	static final String ACTION_SEND_SHOUT = "SendShout";
	static final String ACTION_SEND_RESHOUT = "SendReshout";
	static final String ACTION_SEND_COMMENT = "SendComment";
	static final String ACTION_SEND_RECOMMENT = "SendRecomment";

	static final String ACTION_DETAIL_VIEW = "DetailView";

	static final String ACTION_CREATE_SHOUT = "CreateShout";
	static final String ACTION_CREATE_RESHOUT = "CreateReshout";
	static final String ACTION_CREATE_COMMENT = "CreateComment";
	static final String ACTION_CREATE_RECOMMENT = "CreateRecomment";

	static final String LABEL_CONTENT_DESCRIPTOR = "ContentDescriptor";
	static final String LABEL_CONTENT_REQUEST = "ContentRequest";
	static final String LABEL_MERKEL_NODE = "MerkleNode";
	static final String LABEL_SHOUT = "Shout";
	static final String LABEL_RESHOUT = "Reshout";
	static final String LABEL_COMMENT = "Comment";
	static final String LABEL_RECOMMENT = "Recomment";

	private static Tracker tracker;

	/**
	 * Initializes the EasyTracker using the application context. Should be set
	 * in the onStart method of the application.
	 * 
	 * @param context the application's context (can be an Application object)
	 */
	public static void initialize(Application context) {
		EasyTracker.getInstance().setContext(context);
		tracker = EasyTracker.getTracker();
	}

	static String actionReceive(Shout shout) {
		switch (shout.getType()) {
			case SHOUT:
				return ACTION_RECEIVE_SHOUT;
			case RESHOUT:
				return ACTION_RECEIVE_RESHOUT;
			case COMMENT:
				return ACTION_RECEIVE_COMMENT;
			case RECOMMENT:
				return ACTION_RECEIVE_RECOMMENT;
			default:
				throw new IllegalArgumentException("Unexpected ShoutType: " + shout.getType());
		}
	}

	static String actionCreate(Shout shout) {
		switch (shout.getType()) {
			case SHOUT:
				return ACTION_CREATE_SHOUT;
			case RESHOUT:
				return ACTION_CREATE_RESHOUT;
			case COMMENT:
				return ACTION_CREATE_COMMENT;
			case RECOMMENT:
				return ACTION_CREATE_RECOMMENT;
			default:
				throw new IllegalArgumentException("Unexpected ShoutType: " + shout.getType());
		}
	}

	static String actionSend(Shout shout) {
		switch (shout.getType()) {
			case SHOUT:
				return ACTION_SEND_SHOUT;
			case RESHOUT:
				return ACTION_SEND_RESHOUT;
			case COMMENT:
				return ACTION_SEND_COMMENT;
			case RECOMMENT:
				return ACTION_SEND_RECOMMENT;
			default:
				throw new IllegalArgumentException("Unexpected ShoutType: " + shout.getType());
		}
	}

	static String label(Shout shout) {
		switch (shout.getType()) {
			case SHOUT:
				return LABEL_SHOUT;
			case RESHOUT:
				return LABEL_RESHOUT;
			case COMMENT:
				return LABEL_COMMENT;
			case RECOMMENT:
				return LABEL_RECOMMENT;
			default:
				throw new IllegalArgumentException("Unexpected ShoutType: " + shout.getType());
		}
	}

	// Content Tracking
	public static void trackReceiveContentDescriptor(ContentDescriptor descriptor) {
		tracker.sendEvent(CATEGORY_NETWORK_RECEIVE_EVENT, ACTION_RECEIVE_CONTENT_DESCRIPTOR,
				descriptor.getHash().toString(), (long) 1);
	}

	public static void trackReceiveContentRequest(ContentRequest request) {
		tracker.sendEvent(CATEGORY_NETWORK_RECEIVE_EVENT, ACTION_RECEIVE_CONTENT_REQUEST,
				request.getObjectHash().toString(), (long) 1);
	}

	public static void trackReceiveMerkleNode(MerkleNode node) {
		tracker.sendEvent(CATEGORY_NETWORK_RECEIVE_EVENT, ACTION_RECEIVE_MERKLE_NODE,
				node.getHash().toString(), (long) 1);
	}

	public static void trackSendContentDescriptor(ContentDescriptor descriptor) {
		tracker.sendEvent(CATEGORY_NETWORK_SEND_EVENT, ACTION_SEND_CONTENT_DESCRIPTOR,
				descriptor.getHash().toString(), (long) 1);
	}

	public static void trackSendContentRequest(ContentRequest request) {
		tracker.sendEvent(CATEGORY_NETWORK_SEND_EVENT, ACTION_SEND_CONTENT_REQUEST,
				request.getObjectHash().toString(), (long) 1);
	}

	public static void trackSendMerkleNode(MerkleNode node) {
		tracker.sendEvent(CATEGORY_NETWORK_SEND_EVENT, ACTION_SEND_MERKLE_NODE,
				node.getHash().toString(), (long) 1);
	}

	// Shout Tracking
	public static void trackReceiveShout(Shout shout) {
		tracker.sendEvent(CATEGORY_NETWORK_RECEIVE_EVENT, actionReceive(shout),
				shout.getHash().toString(), (long) 1);
	}

	public static void trackCreateShout(Shout shout) {
		tracker.sendEvent(CATEGORY_UI_EVENT, actionCreate(shout),
				shout.getHash().toString(), (long) 1);
	}

	public static void trackSendShout(Shout shout) {
		tracker.sendEvent(CATEGORY_NETWORK_SEND_EVENT, actionSend(shout),
				shout.getHash().toString(), (long) 1);
	}

	// View Shout tracking
	public static void trackViewDetails(Shout shout) {
		tracker.sendEvent(CATEGORY_UI_EVENT, ACTION_DETAIL_VIEW,
				shout.getHash().toString(), (long) 1);
	}

}
