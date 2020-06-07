package jormCore.DBConnection;

public class FieldWrapper {
	
	private String _name;
	private String _type;
	private boolean _isPrimaryKey;
	private boolean _canNotBeNull;
	private boolean _autoincrement;

	public FieldWrapper(String name, String type, boolean isPrimaryKey, boolean canBeNull, boolean autoincrement)
	{
	_name = name;
	_type = type;
	_isPrimaryKey = isPrimaryKey;
	_canNotBeNull = canBeNull;
	_autoincrement = autoincrement;
	}

	public String get_name() {
		return _name;
	}

	public String get_type() {
		return _type;
	}

	public boolean is_isPrimaryKey() {
		return _isPrimaryKey;
	}

	public boolean is_canNotBeNull() {
		return _canNotBeNull;
	}

	public boolean is_autoincrement() {
		return _autoincrement;
	}
}
