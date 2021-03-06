package cuchaz.enigma.translation.mapping;

import cuchaz.enigma.translation.representation.entry.Entry;

import javax.annotation.Nullable;

public class MappingPair<E extends Entry<?>, M> {
	private final E entry;
	private final M mapping;

	public MappingPair(E entry, @Nullable M mapping) {
		this.entry = entry;
		this.mapping = mapping;
	}

	public MappingPair(E entry) {
		this(entry, null);
	}

	public E getEntry() {
		return entry;
	}

	@Nullable
	public M getMapping() {
		return mapping;
	}
}
