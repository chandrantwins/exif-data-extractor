package photos.waldo;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * Connects to a S3 bucket and lists the files
 * @author luizgerosa
 *
 */
public class S3Connector implements AutoCloseable {

	private AmazonS3Client s3Client;
	private String bucketName;
	private String continuationToken;
	private boolean hasNext;

	/**
	 * Class constructor
	 * @param bucketName The name of the S3 bucket that this class will connect to.
	 */
	public S3Connector(String bucketName) {
		s3Client = new AmazonS3Client(new AnonymousAWSCredentials());
		this.bucketName = bucketName;
	}

	/**
	 * List all files stored in the bucket
	 * @param lastModifiedAt Filters the output by the last modified date (optional)
	 * @return The next batch of file keys retrieved from the S3 bucket (up to 1k). 
	 */
	public List<String> nextBatchOfFiles(Date lastModifiedAt) {


		//TODO: refactoring the key naming in the bucket to include the timestamp of last modification would allow us
		// to use the filter .withStartAfter(startAfter) and process only new files. For now, the filtering will be done
		// in the client side

		final ListObjectsV2Request req = new ListObjectsV2Request()
				.withBucketName(bucketName);

		if (continuationToken != null) {
			req.setContinuationToken(continuationToken);
		}

		ListObjectsV2Result result = s3Client.listObjectsV2(req);
		this.hasNext = result.isTruncated();
		this.continuationToken = result.getNextContinuationToken();

		Stream<S3ObjectSummary> stream = result.getObjectSummaries().stream();
		if (lastModifiedAt != null) {
			stream = stream.filter(f -> f.getLastModified().after(lastModifiedAt));
		}

		return stream.map(f -> f.getKey()).collect(Collectors.toList());
	}

	/**
	 * Gets the input stream containing the content of the file 
	 * @param file
	 * @return
	 */
	public InputStream getContent(String file) {

		GetObjectRequest objRequest = new GetObjectRequest(bucketName, file);
		S3Object object = s3Client.getObject(objRequest);

		return object.getObjectContent();
	}

	/**
	 * Gets whether or not the listing has more batches to be retrieved.
	 * @return The value true if the file listing has more batches to be retrieved.
	 *  Returns the value false if otherwise. When returning true, additional calls to nextBatchOfFiles() is needed in order to obtain more results.
	 */
	public boolean hasNext() {
		return hasNext;
	}

	/**
	 * Gets the AmazonS3Client object associated wih this connector 
	 * @return
	 */
	public AmazonS3Client getS3Client() {
		return this.s3Client;
	}

	/**
	 * Closes this connector
	 */
	@Override
	public void close() throws Exception {
		s3Client.shutdown();
	}

}
