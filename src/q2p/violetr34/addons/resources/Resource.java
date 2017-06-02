package q2p.violetr34.addons.resources;

class Resource {
	ResourceType resourceType;
	String address;
	String name;
	long lastUpdate;

	Resource(final String address, final String name, final String storageName, final long lastUpdate) {
		this.address = address;
		this.name = name;
		resourceType = Resources.getTypeByStorageName(storageName);
		this.lastUpdate = lastUpdate;
	}
}