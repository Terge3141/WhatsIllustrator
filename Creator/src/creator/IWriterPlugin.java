package creator;

import java.time.LocalDateTime;

import messageparser.ImageMessage;
import messageparser.MediaMessage;
import messageparser.MediaOmittedMessage;
import messageparser.TextMessage;

public interface IWriterPlugin {
	/**
	 * Method is invoked before the actual message are appended. This method should
	 * initialize all necessary global variables.
	 */
	void preAppend(WriterConfig config) throws WriterException;

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
	void appendTextMessage(TextMessage textMessage)throws WriterException;
	
	/**
	 * Appends an image message to the document
	 * @param imageMessage Message to be appended
	 * @throws WriterException
	 */
	void appendImageMessage(ImageMessage imageMessage)throws WriterException;
	
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
}
