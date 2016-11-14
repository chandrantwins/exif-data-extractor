package photos.waldo;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Tag;

/**
 * Execute the extraction of Exif data from a single file. This class should run on its own thread.
 * @author luizgerosa
 *
 */
public class ProcessorThread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ProcessorThread.class);

	String fileKey;
	S3Connector s3Connector;

	/**
	 * Class constructor.
	 * @param fileKey S3 file key
	 * @param s3Connector {@link S3Connector} instance that will be used to get the file content.
	 */
	public ProcessorThread(String fileKey, S3Connector s3Connector) {
		this.fileKey = fileKey;
		this.s3Connector = s3Connector;
	}

	/**
	 * Runs the extraction process in 3 steps:
	 * <ol>
	 * 	<li> Retrieves the file content.</li>
	 * 	<li> Extracts the Exif data reading the beginning of the file.</li>
	 * 	<li> Persists the results in the database.</li>
	 * </ol>
	 */
	@Override
	public void run() {

		logger.info("Processing file {}", fileKey);

		try {

			try (InputStream objectData = s3Connector.getContent(fileKey)) {
				Collection<Tag> tags = ExifDataExtractor.extractExifData(objectData);

				if (tags != null) {
					try (Connection conn = ExtractorDAO.getConnection()) {

						try {
							long id = ExtractorDAO.insertFile(conn, fileKey);
							for (Tag tag : tags) {
								ExtractorDAO.insertExifData(conn, id, tag.getTagName(), tag.getDescription());
							}
							conn.commit();
						} catch (SQLException e) {
							conn.rollback();
							throw e;
						}
					}

				}
			}


		} catch (ImageProcessingException e) {
			logger.error("ImageProcessingException occurred while processing file {}: {}",
					fileKey, e.getMessage(), e);

		} catch (AmazonServiceException e) {
			logger.error("AmazonServiceException occurred while processing file {}: {}",
					fileKey, e.getMessage(), e);

		} catch (Exception e) {
			logger.error("Unexpected exception occurred while processing file {}",
					fileKey, e);
		}

	}

}
