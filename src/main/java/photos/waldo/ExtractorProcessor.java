package photos.waldo;

import java.sql.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the extraction process
 * @author luizgerosa
 *
 */
public class ExtractorProcessor {

	private static final Logger logger = LoggerFactory.getLogger(ExtractorProcessor.class);
	private static final int NUMBER_OF_THREADS = 10;
	private static final int EXECUTION_TIMEOUT = 10; //in minutes 

	ExecutorService executorService;

	public ExtractorProcessor() {
		executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
	}

	public void process(String bucketName) {

		logger.info("Starting the process");

		try (S3Connector s3Connector = new S3Connector(bucketName)) {

			Date lastExecutionAt = ExtractorDAO.getLastExecutionDate();
			long currentExecutionAt = System.currentTimeMillis();

			long count = 0;
			do { 
				List<String> files = s3Connector.nextBatchOfFiles(lastExecutionAt);

				if (!files.isEmpty()) {
					logger.info("Processing next {} files", files.size());
					count += files.size();

					for (String file : files) {
						ProcessorThread thread = new ProcessorThread(file, s3Connector);
						executorService.execute(thread);
					}
				} else {
					logger.info("There aren't new files to process");
				}


			} while (s3Connector.hasNext());

			executorService.shutdown();
			if (!executorService.awaitTermination(EXECUTION_TIMEOUT, TimeUnit.MINUTES)) {
				logger.error("Execution timeout");
			}

			ExtractorDAO.saveLog(currentExecutionAt);
			logger.info("Total files processed: {}", count);


		} catch (Exception e) {
			logger.error("Unexpected exception occurred", e);
		}


		logger.info("Finished the process");
	}

}
