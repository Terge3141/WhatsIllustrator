package creator.plugins.fop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import creator.plugins.IWriterPlugin;
import creator.plugins.WriterConfig;
import creator.plugins.WriterException;
import helper.EmojiParser;
import messageparser.ImageMessage;
import messageparser.MediaMessage;
import messageparser.MediaOmittedMessage;
import messageparser.TextMessage;

public class FOPWriterPlugin implements IWriterPlugin {

	private static Logger logger = LogManager.getLogger(FOPWriterPlugin.class);

	private WriterConfig config;
	private EmojiParser emojis;
	private XMLStreamWriter writer;

	private Path xmlOutputPath;
	private Path xslOutputPath;
	private Path incOutputPath;
	private Path pdfOutputPath;

	@Override
	public void preAppend(WriterConfig config) throws WriterException {
		this.config = config;

		this.emojis = new EmojiParser(config.getEmojiList());

		try {
			Path outputDir = this.config.getOutputDir().resolve("fo");
			outputDir.toFile().mkdir();
			this.xmlOutputPath = outputDir.resolve(this.config.getNamePrefix() + ".xml");
			this.xslOutputPath = outputDir.resolve(this.config.getNamePrefix() + ".xsl");
			this.incOutputPath = outputDir.resolve("fopincludes.xsl");
			this.pdfOutputPath = outputDir.resolve(this.config.getNamePrefix() + ".pdf");

			logger.info("Writing output to '{}'", xmlOutputPath);
			FileOutputStream out = new FileOutputStream(xmlOutputPath.toFile());

			// write xsl file
			writeRessourceFile("fopsample.xsl", xslOutputPath);
			writeRessourceFile("fopincludes.xsl", incOutputPath);

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

		try {
			toPDF();
		} catch (FOPException | TransformerException | IOException e) {
			throw new WriterException(e);
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
		FOPTextMessage fopTextMessage = FOPTextMessage.of(textMessage, config.getDateUtils(), this.emojis);
		appendObject(fopTextMessage, this.writer);
	}

	@Override
	public void appendImageMessage(ImageMessage imageMessage) throws WriterException {
		FOPImageMessage fopImageMessage = FOPImageMessage.of(imageMessage, config.getDateUtils(),
				this.config.getImageDir());
		appendObject(fopImageMessage, this.writer);
	}

	@Override
	public void appendMediaOmittedMessage(MediaOmittedMessage mediaOmittedMessage) throws WriterException {
		List<FOPImageMessage> fopImageMessages = FOPImageMessage.of(mediaOmittedMessage, config.getDateUtils(),
				this.config.getImagePoolDir());
		for (FOPImageMessage fopImageMessage : fopImageMessages) {
			appendObject(fopImageMessage, this.writer);
		}
	}

	@Override
	public void appendMediaMessage(MediaMessage mediaMessage) throws WriterException {
		FOPMediaMessage fopMediaMessage = FOPMediaMessage.of(mediaMessage, config.getDateUtils());
		appendObject(fopMediaMessage, this.writer);
	}

	private <T> void appendObject(T obj, XMLStreamWriter writer) throws WriterException {
		try {
			JAXBContext context = JAXBContext.newInstance(obj.getClass());
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.setProperty("jaxb.encoding", "Unicode");
			marshaller.marshal(obj, writer);
		} catch (JAXBException je) {
			throw new WriterException(je);
		}
	}

	private void toPDF() throws FOPException, TransformerException, IOException {
		FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

		logger.info("Writing file to {}", this.pdfOutputPath);

		StreamSource xmlSource = new StreamSource(this.xmlOutputPath.toFile());
		OutputStream out = new FileOutputStream(this.pdfOutputPath.toFile());

		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(new StreamSource(this.xslOutputPath.toFile()));

		Result res = new SAXResult(fop.getDefaultHandler());
		transformer.transform(xmlSource, res);

		out.close();
	}

	public static void main2(String args[]) throws FOPException, IOException, TransformerException {
		File xslFile = new File("/tmp/fopsample.xsl");
		StreamSource xmlSource = new StreamSource(new File("/tmp/fopsample.xml"));
		File pdfFile = new File("/tmp/fopsample.pdf");

		FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		OutputStream out = new FileOutputStream(pdfFile);

		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(new StreamSource(xslFile));

		Result res = new SAXResult(fop.getDefaultHandler());
		transformer.transform(xmlSource, res);

		out.close();
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
