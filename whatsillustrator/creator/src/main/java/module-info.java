module creator {
	exports creator.plugins;
	exports creator;

	requires transitive configurator;
	requires helper;
	requires transitive messageparser;
	requires org.apache.logging.log4j;
}