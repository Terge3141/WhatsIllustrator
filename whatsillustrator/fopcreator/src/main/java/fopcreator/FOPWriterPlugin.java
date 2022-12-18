package fopcreator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.TextStringBuilder;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jcodec.api.JCodecException;

import configurator.Global;
import creator.plugins.IWriterPlugin;
import creator.plugins.WriterException;
import emojicontainer.EmojiContainer;
import jdk.internal.net.http.common.Log;
import messageparser.ImageMessage;
import messageparser.MediaMessage;
import messageparser.MediaOmittedMessage;
import messageparser.TextMessage;
import messageparser.VideoMessage;
import videothumbnails.ThumbnailCreator;
import messageparser.LinkMessage;

public class FOPWriterPlugin implements IWriterPlugin {
	private static Logger logger = LogManager.getLogger(FOPWriterPlugin.class);

	private Global globalConfig;
	private EmojiContainer emojis;
	private Path emojiOutputDir;
	private XMLStreamWriter writer;

	private Path xmlOutputPath;
	private Path xslOutputPath;
	private Path incOutputPath;
	private Path pdfOutputPath;

	private int videoThumbnailCnt = 6;
	
	private Path tmpDir;

	@Override
	public void preAppend(String xmlConfig, Global globalConfig) throws WriterException {
		this.globalConfig = globalConfig;

		try {
			this.emojis = new EmojiContainer();
			
			Path outputDir = this.globalConfig.getOutputDir().resolve(this.globalConfig.getNameSuggestion())
					.resolve("fop");
			outputDir.toFile().mkdirs();
			this.xmlOutputPath = outputDir.resolve(this.globalConfig.getNameSuggestion() + ".xml");
			this.xslOutputPath = outputDir.resolve(this.globalConfig.getNameSuggestion() + ".xsl");
			this.incOutputPath = outputDir.resolve("fopincludes.xsl");
			this.pdfOutputPath = outputDir.resolve(this.globalConfig.getNameSuggestion() + ".pdf");

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
		
		try {
			this.tmpDir = Files.createTempDirectory("foptempdir");
			this.emojiOutputDir = this.tmpDir.resolve("emojis");
			writeFopIncludes(incOutputPath);
		} catch (IOException e) {
			throw new WriterException(e);
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
		
		try {
			FileUtils.deleteDirectory(tmpDir.toFile());
		} catch (IOException e) {
			throw new WriterException(e);
		}
	}

	@Override
	public void appendDateHeader(LocalDateTime timepoint) throws WriterException {
		try {
			this.writer.writeStartElement("date");
			this.writer.writeCharacters(globalConfig.getDateUtils().formatDateString(timepoint));
			this.writer.writeEndElement();
		} catch (XMLStreamException xse) {
			throw new WriterException(xse);
		}

	}

	@Override
	public void appendTextMessage(TextMessage textMessage) throws WriterException {
		FOPTextMessage fopTextMessage;
		try {
			fopTextMessage = FOPTextMessage.of(textMessage, globalConfig.getDateUtils(), this.emojis, this.emojiOutputDir);
		} catch (IOException e) {
			throw new WriterException(e);
		}
		appendObject(fopTextMessage, this.writer);
	}

	@Override
	public void appendImageMessage(ImageMessage imageMessage) throws WriterException {
		FOPImageMessage fopImageMessage = FOPImageMessage.of(imageMessage, globalConfig.getDateUtils()
				, this.emojis);
		appendObject(fopImageMessage, this.writer);
	}
	
	@Override
	public void appendVideoMessage(VideoMessage msg) throws WriterException {
		Path videoPath = msg.getFilepath();
		if(videoPath.toFile().exists()) {
			ThumbnailCreator tc = ThumbnailCreator.of(videoPath, videoThumbnailCnt , tmpDir);;
			List<Path> tnPaths;
			try {
				tnPaths = tc.createThumbnails();
			} catch (IOException | JCodecException e) {
				throw new WriterException(e);
			}
			
			List<FOPImageMessage> fopImageMessages = FOPImageMessage.of(msg, globalConfig.getDateUtils(), tnPaths);
			for (FOPImageMessage fopImageMessage : fopImageMessages) {
				appendObject(fopImageMessage, this.writer);
			}
	
		} else {
			logger.warn("File '{}' does not exist, skipping message", videoPath);
		}
	}

	@Override
	public void appendMediaOmittedMessage(MediaOmittedMessage mediaOmittedMessage) throws WriterException {
		List<FOPImageMessage> fopImageMessages = FOPImageMessage.of(mediaOmittedMessage, globalConfig.getDateUtils());
		for (FOPImageMessage fopImageMessage : fopImageMessages) {
			appendObject(fopImageMessage, this.writer);
		}
	}

	@Override
	public void appendMediaMessage(MediaMessage mediaMessage) throws WriterException {
		FOPMediaMessage fopMediaMessage = FOPMediaMessage.of(mediaMessage, globalConfig.getDateUtils());
		appendObject(fopMediaMessage, this.writer);
	}
	
	@Override
	public void appendLinkMessage(LinkMessage linkMessage) throws WriterException {
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
		//throw new WriterException("Problem");
	}

	private void toPDF() throws FOPException, IOException, TransformerException {
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

		System.out.println("rn: " + ressourceName);

		int nRead;
		byte[] data = new byte[1024];
		while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
			fileOutputStream.write(data, 0, nRead);
		}

		fileOutputStream.close();
	}
	
	private void writeFopIncludes(Path dst) throws IOException {
		Files.createDirectory(this.emojiOutputDir);
		
		TextStringBuilder sb = new TextStringBuilder();
		sb.appendln("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.appendln("");
		sb.appendln("<!-- Generated by FOPWriterPlugin -->");
		sb.appendln("");
		
		sb.appendln("<xsl:stylesheet version=\"1.1\"");
		sb.appendln("\txmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"");
		sb.appendln("\txmlns:fo=\"http://www.w3.org/1999/XSL/Format\"");
		sb.appendln("\texclude-result-prefixes=\"fo\">");
		sb.appendln("");
		sb.appendln(String.format("<xsl:variable name=\"emojisprefix\">%s</xsl:variable>", this.emojiOutputDir));
		sb.appendln("");
		sb.appendln("</xsl:stylesheet>");
		
		FileWriter out = new FileWriter(dst.toFile());
		out.write(sb.toString());
		out.close();
	}
}
