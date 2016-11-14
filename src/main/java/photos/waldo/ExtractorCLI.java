package photos.waldo;

/**
 * Extractor Command Line Interface.
 * <p>Usage: java exif-data-extractor [BUCKET NAME]</p>
 * @author luizgerosa
 *
 */
public class ExtractorCLI {


	public static void main(String[] args) {


		if (args.length != 1) {
			System.out.println("Usage: java exif-data-extractor [BUCKET NAME]");
			return;
		}

		ExtractorProcessor processor = new ExtractorProcessor();
		processor.process(args[0]);
	}

}
