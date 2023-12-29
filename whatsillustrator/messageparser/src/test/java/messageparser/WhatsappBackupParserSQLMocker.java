package messageparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class WhatsappBackupParserSQLMocker {
	private Path sqlDir;
	private Path sqliteDBPath;
	private Connection con;
	
	public WhatsappBackupParserSQLMocker(Path sqlDir) throws SQLException, IOException {
		this.sqlDir = sqlDir;
		Files.createDirectories(sqlDir);
		
		this.sqliteDBPath = this.sqlDir.resolve("msgstore.db");
		if(this.sqliteDBPath.toFile().exists()) {
			this.sqliteDBPath.toFile().delete();
		}
		
		this.con = createSqlDatabase(sqliteDBPath);
	}
	
	public Connection getConnection() {
		return this.con;
	}
	
	public Path getSqliteDBPath() {
		return this.sqliteDBPath;
	}
	
	private Connection createSqlDatabase(Path sqliteDBPath) throws SQLException {
		String url = String.format("jdbc:sqlite:%s", sqliteDBPath);
		Connection con = DriverManager.getConnection(url);
		
		String sql = null;
		
		// v_chats
		sql = "CREATE TABLE v_messages (\n"
				+ "messageid INTEGER,\n"
				+ "chatname TEXT,\n"
				+ "sendername TEXT,\n"
				+ "type_description TEXT,\n"
				+ "text TEXT,\n"
				+ "timestamp INTEGER\n"
				+ ");";
		invokeSQL(con, sql);
		
		// media
		sql = "CREATE TABLE message_media (\n"
				+ "message_row_id INTEGER,\n"
				+ "file_path TEXT\n"
				+ ");";
		invokeSQL(con, sql);
		
		return con;
	}
	
	private boolean invokeSQL(Connection con, String sql) throws SQLException {
		Statement stmt = con.createStatement();
		return stmt.execute(sql);
	}
}

