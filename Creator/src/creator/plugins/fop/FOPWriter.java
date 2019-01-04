package creator.plugins.fop;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import creator.plugins.IWriterPlugin;
import creator.plugins.WriterConfig;
import creator.plugins.WriterException;
import helper.DateUtils;
import messageparser.ImageMessage;
import messageparser.MediaMessage;
import messageparser.MediaOmittedMessage;
import messageparser.TextMessage;

public class FOPWriter implements IWriterPlugin {

	private WriterConfig config;
	private XMLStreamWriter writer;

	private static Logger logger = LogManager.getLogger(FOPWriter.class);

	@Override
	public void preAppend(WriterConfig config) throws WriterException {
		this.config = config;

		try {
			// TODO change
			FileOutputStream out = new FileOutputStream("/tmp/bla.xml");

			this.writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-16");
			this.writer.writeStartDocument();
			this.writer.writeStartElement("messages");
		} catch (IOException ioe) {
			throw new WriterException(ioe);
		} catch (XMLStreamException xse) {
			throw new WriterException(xse);
		} catch (FactoryConfigurationError fce) {
			throw new WriterException(fce);
		}
	}

	@Override
	public void postAppend() throws WriterException {
		try {
			this.writer.writeEndElement();
			this.writer.writeEndDocument();

			this.writer.flush();
		} catch (XMLStreamException xse) {
			throw new WriterException(xse);
		}
	}

	@Override
	public void appendDateHeader(LocalDateTime timepoint) throws WriterException {
		try {
			this.writer.writeStartElement("date");
			this.writer.writeCharacters(config.getDateUtils().formatDateString(LocalDateTime.now()));
			this.writer.writeEndElement();
		} catch (XMLStreamException xse) {
			throw new WriterException(xse);
		}

	}

	@Override
	public void appendTextMessage(TextMessage textMessage) throws WriterException {
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(FOPTextMessage.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			FOPTextMessage fopTextMessage = new FOPTextMessage(textMessage, config.getDateUtils());
			marshaller.marshal(fopTextMessage, writer);
		} catch (JAXBException je) {
			throw new WriterException(je);
		}
	}

	@Override
	public void appendImageMessage(ImageMessage imageMessage) throws WriterException {
		logger.warn("Not implemented yet");
	}

	@Override
	public void appendMediaOmittedMessage(MediaOmittedMessage mediaOmittedMessage) throws WriterException {
		logger.warn("Not implemented yet");
	}

	@Override
	public void appendMediaMessage(MediaMessage mediaMessage) throws WriterException {
		logger.warn("Not implemented yet");
	}

	public static void main2(String args[])
			throws JAXBException, IOException, XMLStreamException, FactoryConfigurationError {
		TextMessage message1 = new TextMessage(LocalDateTime.now(), "Firstname surname", "Blabla");
		TextMessage message2 = new TextMessage(LocalDateTime.now(), "Firstname surname2", "Blabla2");
		DateUtils dateUtils = new DateUtils(Locale.GERMAN);

		FOPTextMessage fopTextMessage1 = new FOPTextMessage(message1, dateUtils);
		FOPTextMessage fopTextMessage2 = new FOPTextMessage(message2, dateUtils);

		JAXBContext context = JAXBContext.newInstance(FOPTextMessage.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-8");
		writer.writeStartDocument();
		writer.writeStartElement("messages");

		marshaller.marshal(fopTextMessage1, writer);

		writer.writeStartElement("date");
		writer.writeCharacters(dateUtils.formatDateString(LocalDateTime.now()));
		writer.writeEndElement();

		marshaller.marshal(fopTextMessage2, writer);

		writer.writeEndElement();
		writer.writeEndDocument();

		writer.flush();

		// File file = new File("/tmp/bla.xml");
		// marshaller.mas
		// marshaller.marshal(fopTextMessage1, out);
		// marshaller.marshal(fopTextMessage2, out);
		Files.write(Paths.get("/tmp/bla.xml"), out.toByteArray());
		System.out.println("Moin");
	}

}
