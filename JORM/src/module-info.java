module jorm {
	requires java.sql;
	exports jormCore;
	exports jormCore.Annotaions;
	exports jormCore.DBConnection;
	exports jormCore.Tracing;
	exports jormCore.Wrapping;
}