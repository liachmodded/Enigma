package cuchaz.enigma.translation.mapping.serde;

import cuchaz.enigma.translation.mapping.AccessModifier;
import cuchaz.enigma.translation.mapping.EntryMapping;
import joptsimple.internal.Strings;

final class RawEntryMapping {
	private final String targetName;
	private final AccessModifier access;

	RawEntryMapping(String targetName) {
		this(targetName, null);
	}

	RawEntryMapping(String targetName, AccessModifier access) {
		this.access = access;
		this.targetName = targetName;
	}

	EntryMapping bake() {
		return Strings.isNullOrEmpty(targetName) ? null : new EntryMapping(targetName, access);
	}
}
