module messageparser {
	exports messageparser;

	requires com.google.gson;
	requires transitive configurator;
	requires helper;
	requires imagematcher;
	requires java.sql;
	requires org.apache.commons.text;
	requires org.apache.logging.log4j;
	requires signalbackupreader;
	requires java.xml;
}