package de.foconis.core.transponder;

public abstract class TransponderData implements Comparable<TransponderData> {

	public abstract String getAppId();

	public abstract String getVersion();

	public String getDbPath() {
		return null;
	}

	@Override
	public int compareTo(final TransponderData other) {
		// TODO Auto-generated method stub
		String s1 = getDbPath().replaceAll("(?i)foconis", "");
		String s2 = other.getDbPath().replaceAll("(?i)foconis", "");
		int i1 = s1.length();
		int i2 = s2.length();

		if (i1 == i2) {
			return s1.compareTo(s2);
		}
		return i2 - i1;
	}

	@Override
	public String toString() {
		return "TransponderData [getAppId()=" + getAppId() + ", getVersion()=" + getVersion() + ", getDbPath()=" + getDbPath() + "]";
	}

}
