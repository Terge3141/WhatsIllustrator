package creator;

import helper.DateUtils;

import messageparser.*;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import configurator.Global;
import creator.plugins.IWriterPlugin;
import creator.plugins.WriterException;

public class BookCreator {

	private static Logger logger = LogManager.getLogger(BookCreator.class);

	private Global globalConfig;
	private IParser parser;
	private List<IWriterPlugin> plugins;

	public BookCreator(Global globalConfig, IParser parser, List<IWriterPlugin> writerPlugins) {
		this.globalConfig = globalConfig;
		this.parser = parser;
		this.plugins = writerPlugins;
	}

	
	public void write() throws IOException, ParseException, WriterException  {
		// write messages
		logger.info("Start parsing messages");
		IMessage msg;
		LocalDateTime last = LocalDateTime.MIN;
		while ((msg = parser.nextMessage()) != null) {
			if (DateUtils.dateDiffer(last, msg.getTimepoint())) {
				for (IWriterPlugin plugin : this.plugins) {
					plugin.appendDateHeader(msg.getTimepoint());
				}
			}

			last = msg.getTimepoint();

			if (msg instanceof TextMessage) {
				for (IWriterPlugin plugin : this.plugins) {
					plugin.appendTextMessage((TextMessage) msg);
				}
			} else if (msg instanceof ImageMessage) {
				for (IWriterPlugin plugin : this.plugins) {
					plugin.appendImageMessage((ImageMessage) msg);
				}
			} else if (msg instanceof MediaOmittedMessage) {
				for (IWriterPlugin plugin : this.plugins) {
					plugin.appendMediaOmittedMessage((MediaOmittedMessage) msg);
				}
			} else if (msg instanceof MediaMessage) {
				for (IWriterPlugin plugin : this.plugins) {
					plugin.appendMediaMessage((MediaMessage) msg);
				}
			} else if (msg instanceof LinkMessage) {
				for (IWriterPlugin plugin : this.plugins) {
					plugin.appendLinkMessage((LinkMessage) msg);
				}
			}
		}
		
		// post append
		for (IWriterPlugin plugin : this.plugins) {
			plugin.postAppend();
		}
	}
}
