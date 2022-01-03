package videothumbnails;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

public class ThumbnailCreator {
	
	private Path videoPath;
	private int thumbnailCnt = 5;
	private Path outputDir;
	
	private int width = -1;
	private int height = -1;
	
	public static ThumbnailCreator of(Path videoPath, int thumbnailCnt, Path outputDir) {
		return new ThumbnailCreator(videoPath, thumbnailCnt, outputDir);
	}
	
	protected ThumbnailCreator(Path videoPath, int thumbnailCnt, Path outputDir) {
		this.videoPath = videoPath;
		this.thumbnailCnt = thumbnailCnt;
		this.outputDir = outputDir;
	}

	public List<Path> createThumbnails() throws FileNotFoundException, IOException, JCodecException {
		FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoPath.toFile()));
		int total = grab.getVideoTrack().getMeta().getTotalFrames();
		
		List<Path> thumbnails = new ArrayList<Path>();
		
		for(int i=0; i<this.thumbnailCnt; i++) {
			int frameNr = i * total / this.thumbnailCnt;
			grab.seekToFrameSloppy(frameNr);
			Picture picture = grab.getNativeFrame();
			BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
			
			if(this.width==-1) {
				this.width = picture.getWidth();
				this.height = picture.getHeight();
			}
			
			Path jpgPath = outputDir.resolve(String.format("%s-dump-%04d.jpg", videoPath.getFileName(), i));
	    	ImageIO.write(bufferedImage, "jpg", jpgPath.toFile());
	    	thumbnails.add(jpgPath);
		}
		
		return thumbnails;
	}

	public int getWidth() {
		if(width==-1) {
			throw new IllegalArgumentException("Thumbnails need to be created first");
		}
		
		return width;
	}

	public int getHeight() {
		if(height==-1) {
			throw new IllegalArgumentException("Thumbnails need to be created first");
		}
		
		return height;
	}
	
	public boolean isLandScape() {
		return getWidth() >= getHeight();
	}
}
