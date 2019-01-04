package creator.plugins.fop;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
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

public class FOPWriterPlugin implements IWriterPlugin {

	private WriterConfig config;
	private XMLStreamWriter writer;

	private static Logger logger = LogManager.getLogger(FOPWriterPlugin.class);

	@Override
	public void preAppend(WriterConfig config) throws WriterException {
		this.config = config;

		try {
			Path outputDir = this.config.getOutputDir().resolve("fo");
			outputDir.toFile().mkdir();
			Path xmlOutputPath = outputDir.resolve(this.config.getNamePrefix() + ".xml");
			Path xslOutputPath = outputDir.resolve(this.config.getNamePrefix() + ".xsl");

			logger.info("Writing output to '{}'", xmlOutputPath);
			FileOutputStream out = new FileOutputStream(xmlOutputPath.toFile());

			// write xsl file
			writeRessourceFile("fopsample.xsl", xslOutputPath);

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
			this.writer.writeCharacters(config.getDateUtils().formatDateString(timepoint));
			this.writer.writeEndElement();
		} catch (XMLStreamException xse) {
			throw new WriterException(xse);
		}

	}

	@Override
	public void appendTextMessage(TextMessage textMessage) throws WriterException {
		try {
			JAXBContext context = JAXBContext.newInstance(FOPTextMessage.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			FOPTextMessage fopTextMessage = FOPTextMessage.of(textMessage, config.getDateUtils());
			marshaller.marshal(fopTextMessage, writer);
		} catch (JAXBException je) {
			throw new WriterException(je);
		}
	}

	@Override
	public void appendImageMessage(ImageMessage imageMessage) throws WriterException {
		try {
			JAXBContext context = JAXBContext.newInstance(FOPImageMessage.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			FOPImageMessage fopImageMessage = FOPImageMessage.of(imageMessage, config.getDateUtils(),
					this.config.getImageDir());
			marshaller.marshal(fopImageMessage, writer);
		} catch (JAXBException je) {
			throw new WriterException(je);
		}
	}

	@Override
	public void appendMediaOmittedMessage(MediaOmittedMessage mediaOmittedMessage) throws WriterException {
		try {
			JAXBContext context = JAXBContext.newInstance(FOPImageMessage.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			List<FOPImageMessage> fopImageMessages = FOPImageMessage.of(mediaOmittedMessage, config.getDateUtils(),
					this.config.getImagePoolDir());
			for (FOPImageMessage fopImageMessage : fopImageMessages) {
				marshaller.marshal(fopImageMessage, writer);
			}
		} catch (JAXBException je) {
			throw new WriterException(je);
		}
	}

	@Override
	public void appendMediaMessage(MediaMessage mediaMessage) throws WriterException {
		try {
			JAXBContext context = JAXBContext.newInstance(FOPMediaMessage.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			FOPMediaMessage fopMediaMessage = FOPMediaMessage.of(mediaMessage, config.getDateUtils());
			marshaller.marshal(fopMediaMessage, writer);
		} catch (JAXBException je) {
			throw new WriterException(je);
		}
	}

	public static void main2(String args[])
			throws JAXBException, IOException, XMLStreamException, FactoryConfigurationError {
		TextMessage message1 = new TextMessage(LocalDateTime.now(), "Firstname surname", "Blabla");
		TextMessage message2 = new TextMessage(LocalDateTime.now(), "Firstname surname2", "Blabla2");
		DateUtils dateUtils = new DateUtils(Locale.GERMAN);

		FOPTextMessage fopTextMessage1 = FOPTextMessage.of(message1, dateUtils);
		FOPTextMessage fopTextMessage2 = FOPTextMessage.of(message2, dateUtils);

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

	private void writeRessourceFile(String ressourceName, Path destPath) throws IOException {
		InputStream inputStream = this.getClass().getResourceAsStream(ressourceName);
		FileOutputStream fileOutputStream = new FileOutputStream(destPath.toFile());

		int nRead;
		byte[] data = new byte[1024];
		while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
			fileOutputStream.write(data, 0, nRead);
		}
		
		fileOutputStream.close();
	}
}
