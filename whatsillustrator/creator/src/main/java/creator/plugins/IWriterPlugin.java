package creator.plugins;

import java.time.LocalDateTime;

import configurator.Global;
import messageparser.ImageMessage;
import messageparser.MediaMessage;
import messageparser.MediaOmittedMessage;
import messageparser.TextMessage;
import messageparser.VideoMessage;
import messageparser.LinkMessage;

public interface IWriterPlugin {
	/**
	 * Method is invoked before the actual messages are appended. This method should
	 * initialize all necessary global variables.
	 */
	void preAppend(String xmlConfig, Global globalConfig) throws WriterException;

	/**
	 * Method is invoked after all messages are appended. This method should save
	 * all necessary data.
	 */
	void postAppend() throws WriterException;
	
	/**
	 * Appends a date header to the document
	 * @param dateStr Date to be appended
	 * @throws WriterException
	 */
	void appendDateHeader(LocalDateTime timepoint) throws WriterException;
	
	/**
	 * Appends a text message to the document
	 * @param textMessage Message to be appended
	 * @throws WriterException
	 */
	void appendTextMessage(TextMessage textMessage) throws WriterException;
	
	/**
	 * Appends an image message to the document
	 * @param imageMessage Message to be appended
	 * @throws WriterException
	 */
	void appendImageMessage(ImageMessage imageMessage) throws WriterException;
	
	/**
	 * Appends a video message to the document
	 * @param videoMessage Message to be appended
	 * @throws WriterException
	 */
	void appendVideoMessage(VideoMessage videoMessage) throws WriterException;
	
	/**
	 * Appends a media omitted message to the document
	 * @param mediaOmittedMessage Message to be appended
	 * @throws WriterException
	 */
	void appendMediaOmittedMessage(MediaOmittedMessage mediaOmittedMessage)throws WriterException;
	
	/**
	 * Appends a media message to the document
	 * @param mediaMessage Message to be appended
	 * @throws WriterException
	 */
	void appendMediaMessage(MediaMessage mediaMessage)throws WriterException;
	
	/**
	 * Appends a link message to the document
	 * @param linkMessage Message to be appended
	 * @throws WriterException
	 */
	void appendLinkMessage(LinkMessage linkMessage)throws WriterException;
}
