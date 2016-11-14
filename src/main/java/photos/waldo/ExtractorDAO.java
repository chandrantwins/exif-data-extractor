package photos.waldo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to persist and retrieve data from the database
 * @author luizgerosa
 *
 */
public class ExtractorDAO {

	private static final Logger logger = LoggerFactory.getLogger(ExtractorDAO.class);

	public static Connection getConnection() throws SQLException {

		String url = System.getenv("WALDO_PHOTOS_JDBC_URL");
		String user = System.getenv("WALDO_PHOTOS_JDBC_USER");
		String password = System.getenv("WALDO_PHOTOS_JDBC_PASSWORD");

		logger.debug("Connection to database {}", url);

		Connection conn = DriverManager.getConnection(url, user, password);
		conn.setAutoCommit(false);

		return conn;
	}

	public static long insertFile(Connection connection, String fileKey) throws SQLException {

		String sql = "insert into file (file_id, key) values (nextval('seq'), ?)";

		try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, fileKey);
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				rs.next();
				return rs.getLong(1);
			}
		}
	}

	public static void insertExifData(Connection connection, Long fileId, String name, String value) throws SQLException {

		String sql = "insert into exif_data (exif_data_id, file_id, name, value) values (nextval('seq'), ?, ?, ?)";

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, fileId);
			ps.setString(2, name);
			ps.setString(3, value);
			ps.execute();
		}
	}

	public static Date getLastExecutionDate() throws SQLException {

		try (Connection conn = getConnection()) {
			String sql = "select max(executed_at) from log";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						return rs.getDate(1);
					}
				}
			}
		}

		return null;
	}

	public static void saveLog(long executedAt) throws SQLException {
		try (Connection conn = getConnection()) {
			String sql = "insert into log (log_id, executed_at) values (nextval('seq'), ?)";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setTimestamp(1, new Timestamp(executedAt));
				ps.execute();
				conn.commit();
			}
		}
	}
}
