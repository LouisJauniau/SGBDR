import java.io.BufferedReader;
import java.io.FileReader;

package up.mi.jgm.dbconfig;


public class DBConfig{
	private String dbpath;
	
	public DBConfig(String dbpath) {
		this.dbpath = dbpath;
	}
	
	public static DBConfig LoadDBConfig(String fichier_config) {
		
		BufferReader br = new BufferReader(new FileReader(fichier_config));
		return
	}
}