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
import messageparser.ImageMessage;
import messageparser.MediaMessage;
import messageparser.MediaOmittedMessage;
import messageparser.TextMessage;

public class FOPWriterPlugin implements IWriterPlugin {

	private static Logger logger = LogManager.getLogger(FOPWriterPlugin.class);

	private WriterConfig config;
	private XMLStreamWriter writer;

	private Path xmlOutputPath;
	private Path xslOutputPath;
	private Path pdfOutputPath;

	@Override
	public void preAppend(WriterConfig config) throws WriterException {
		this.config = config;

		try {
			Path outputDir = this.config.getOutputDir().resolve("fo");
			outputDir.toFile().mkdir();
			this.xmlOutputPath = outputDir.resolve(this.config.getNamePrefix() + ".xml");
			this.xslOutputPath = outputDir.resolve(this.config.getNamePrefix() + ".xsl");
			this.pdfOutputPath = outputDir.resolve(this.config.getNamePrefix() + ".pdf");

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
