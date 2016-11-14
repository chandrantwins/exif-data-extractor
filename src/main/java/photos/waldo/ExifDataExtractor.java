package photos.waldo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.exif.ExifSubIFDDirectory;

/**
 * Helper class to extract Exif data from JPEG images
 * @author luizgerosa
 *
 */
public class ExifDataExtractor {

	private static final Logger logger = LoggerFactory.getLogger(ExifDataExtractor.class);

	public static Collection<Tag> extractExifData(InputStream is) throws IOException, JpegProcessingException {


		BufferedInputStream bufferedInputStream = new BufferedInputStream(is);

		FileType fileType = FileTypeDetector.detectFileType(bufferedInputStream);

		// If additional file types are necessary (e.g. TIFF), add new conditionals
		if (FileType.Jpeg.equals(fileType)) {
			Metadata metadata = JpegMetadataReader.readMetadata(bufferedInputStream, Arrays.asList(new ExifReader()));

			// Only extracting ExifSubIFDDirectory
			Directory dir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			if (dir != null) { 
				return dir.getTags();
			}

		} else {

			logger.warn("{} file format is not supported. Only JPEG file format is supported.", fileType);
		}

		return null;
	}
}
