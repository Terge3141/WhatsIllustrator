module odfcreator {
	exports odfcreator;

	requires transitive configurator;
	requires creator;
	requires emojicontainer;
	requires helper;
	requires jcodecmerged;
	requires transitive messageparser;
	requires odfdom.java;
	requires org.apache.commons.io;
	requires org.apache.logging.log4j;
	requires simple.odf;
	requires videothumbnails;
}
