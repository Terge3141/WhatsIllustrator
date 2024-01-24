package messageparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SignalParserSQLMocker {
	private Path sqlDir;
	private Path sqliteDBPath;
	private Connection con;
	
	public SignalParserSQLMocker(Path sqlDir) throws SQLException, IOException {
		this.sqlDir = sqlDir;
		Files.createDirectories(sqlDir);
		
		this.sqliteDBPath = this.sqlDir.resolve("database.sqlite");
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
		sql = "CREATE TABLE v_chats (\n"
				+ "msgid INTEGER,\n"
				+ "date INTEGER,\n"
				+ "sender TEXT,\n"
				+ "chatname TEXT,\n"
				+ "text TEXT,\n"
				+ "type TEXT\n"
				+ ");";
		invokeSQL(con, sql);
		
		// part
		sql = "CREATE TABLE part (\n"
				+ "mid INTEGER,\n"
				+ "ct TEXT,\n"
				+ "unique_id INTEGER,\n"
				+ "sticker_id INTEGER\n"
				+ ");";
		invokeSQL(con, sql);
		
		// v_stickers
		sql = "CREATE TABLE v_stickers (\n"
				+ "file_id INTEGER,\n"
				+ "mid	 INTEGER,\n"
				+ "ct TEXT,\n"
				+ "sticker_emoji STRING\n"
				+ ");";
		invokeSQL(con, sql);
		
		return con;
	}
	
	private boolean invokeSQL(Connection con, String sql) throws SQLException {
		Statement stmt = con.createStatement();
		return stmt.execute(sql);
	}
}
