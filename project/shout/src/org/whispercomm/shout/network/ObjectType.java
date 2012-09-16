
package org.whispercomm.shout.network;

public enum ObjectType {
	Shout(0x00), Content(0x01);

	/**
	 * Maximum value of the object type identifier.
	 */
	public static final int MAX_TYPE_ID;

	private static final ObjectType[] ALL_TYPES;

	static
	{
		// Determine the maximum type id
		int maxId = 0;
		for (ObjectType t : ObjectType.values()) {
			maxId = (t.id > maxId) ? t.id : maxId;
		}
		MAX_TYPE_ID = maxId;

		// Configure that mapping from type id to enum
		ALL_TYPES = new ObjectType[MAX_TYPE_ID + 1];
		for (ObjectType t : ObjectType.values()) {
			ALL_TYPES[t.id] = t;
		}
	}

	private final int id;

	private ObjectType(int id) {
		if (!(0 <= id && id < 256)) {
			throw new IllegalArgumentException("ObjectType id must be in range [0,256).");
		}
		this.id = id;
	}

	/**
	 * Returns the identifier for this type. Identifiers are in the range
	 * [0,256) and thus will fit in an unsigned byte.
	 * 
	 * @return the identifier
	 */
	public int getId() {
		return id;
	}

	public static ObjectType fromId(int id) throws IllegalArgumentException {
		if (!(0 <= id && id <= MAX_TYPE_ID))
			throw new IllegalArgumentException();

		ObjectType ret = ALL_TYPES[id];
		if (ret == null)
			throw new IllegalArgumentException();

		return ret;
	}
}
