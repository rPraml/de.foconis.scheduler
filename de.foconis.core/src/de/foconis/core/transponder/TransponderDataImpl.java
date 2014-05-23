package de.foconis.core.transponder;

public class TransponderDataImpl extends TransponderData {
	private final String appId;
	private final String dbPath;
	private final String version;

	public TransponderDataImpl(final TransponderData data, final String databasePath) {
		appId = data.getAppId();
		dbPath = databasePath;
		version = data.getVersion();
	}

	@Override
	public String getAppId() {
		return appId;
	}

	@Override
	public String getDbPath() {
		return dbPath;
	}

	@Override
	public String getVersion() {
		return version;
	}

}
