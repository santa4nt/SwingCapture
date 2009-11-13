package flickr;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
//import javax.swing.UIManager;


/**
 * Taken from http://trac.assembla.com/flickr/browser/src
 */
public class Tools {
	
	public static String md5(String key) {
		byte[] uniqueKey = key.getBytes();
		byte[] hash = null;

		try {
			hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
		} catch (NoSuchAlgorithmException e) {
			System.err.println("no MD5 support in this VM");
		}
		StringBuffer hashString = new StringBuffer();
		for (int i = 0; i < hash.length; ++i) {
			String hex = Integer.toHexString(hash[i]);
			if (hex.length() == 1) {
				hashString.append('0');
				hashString.append(hex.charAt(hex.length() - 1));
			} else {
				hashString.append(hex.substring(hex.length() - 2));
			}
		}
		return hashString.toString();
	}
	
	public static String getWebpage(String url) {
		String webpage = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
			String line = reader.readLine();
			while (line != null) {
				webpage = webpage + "\n" + line;
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return webpage;
	}
	
	public static String postWebpage(String url, String parameter_name, File file) {
		Map<String, File> files = new TreeMap<String, File>();
		files.put(parameter_name, file);
		return postWebpage(url, files);
	}
	
	public static String postWebpage(String url, Map<String, File> files) {
		String webpage = "";
		try {
			//Open connection for posting
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			String boundary = "---------------------------7d273f7a0d3";
	        conn.setDoOutput(true);
	        //conn.setDoInput(true);
	        //urlConn.setUseCaches(false);
			//urlConn.setDefaultUseCaches(false);
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
	        conn.connect();
	        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
	        out.writeBytes("--" + boundary + "\r\n");
	        
	        //Write in url parameters (which are string)
			int i = url.indexOf("?");
			if (i != -1) {
				if (i+1 != url.length()) {
					url = url.substring(i+1);
					String[] keysvalues = url.split("&");
					for (int j = 0; j < keysvalues.length; j++) {
						String[] kv = keysvalues[j].split("=");
					    out.writeBytes("Content-Disposition: form-data; name=\"" + kv[0] + "\"\r\n\r\n");
					    out.write(kv[1].getBytes("UTF-8"));
					    out.writeBytes("\r\n" + "--" + boundary + "\r\n");			
					}
				}
			}

			//Write - upload files
			for (Iterator<Map.Entry<String, File>> it = files.entrySet().iterator(); it.hasNext(); ) {
		        Map.Entry<String, File> entry = it.next();
		        String name = entry.getKey();
		        File file = entry.getValue();
		        if (file.exists() && file.isFile() && file.canRead()) {
					InputStream in = new BufferedInputStream(new FileInputStream(file));
					out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\""+file.getName()+"\";\r\n");
					String mtype = new MimetypesFileTypeMap().getContentType(file);
					out.writeBytes("Content-Type: " + mtype + "\r\n\r\n");
					byte[] data = new byte[1024];
					int r = 0;
					while((r = in.read(data, 0, data.length)) != -1) {
						out.write(data, 0, r);
					}					
					in.close();
					out.writeBytes("\r\n" + "--" + boundary + "\r\n");
				}
			}
	        out.flush();
	        out.close();
	        
	        //Read response from server
	        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = reader.readLine();
			while (line != null) {
				webpage = webpage + "\n" + line;
				line = reader.readLine();
			}
	        conn.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return webpage;
	}
	
	//<token>45-76598454353455</token>
	public static String getXmlValue(String key, String xml) {
		String begin_key = "<" + key + ">";
		String end_key = "</" + key + ">";
		int begin_index = xml.indexOf(begin_key);
		int end_index = xml.indexOf(end_key);
		if ((begin_index == -1) || (end_index == -1)) return "";
		return xml.substring(begin_index+begin_key.length(), end_index);
	}
	
	//<user nsid="12037949754@N01" username="Bees" fullname="Cal H" />
	public static String getXmlParameter(String primary_key, String secondary_key, String xml) {
		primary_key = "<" + primary_key;
		int primary_index = xml.indexOf(primary_key);
		if (primary_index == -1) return "";
		xml = xml.substring(primary_index);
		secondary_key = secondary_key + "=\"";
		int secondary_index = xml.indexOf(secondary_key);
		if (secondary_index == -1) return "";
		xml = xml.substring(secondary_index + secondary_key.length());
		int end_index = xml.indexOf("\"");
		if (end_index == -1) return "";
		return xml.substring(0, end_index);
	}
	
	public static String getXmlParameterWithLineContain(String primary_key, String secondary_key, String contain, String xml) {
		String pkey = "<" + primary_key;
		String[] lines = xml.split("\n");
		for (int i = 0; i < lines.length; i++) {
			if ((lines[i].indexOf(pkey) != -1) && (lines[i].indexOf(contain) != -1)) {
				return getXmlParameter(primary_key, secondary_key, lines[i]);
			}
		}
		return "";
	}
	
	public static void dispWindow(String title, String message, int type) {
//	    try {
//	    	String nativeLF = UIManager.getSystemLookAndFeelClassName();
//	    	UIManager.setLookAndFeel(nativeLF);
//	    } catch (Exception e) {
//          }

            JFrame frame = new JFrame(title);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            JOptionPane.showMessageDialog(frame, message, title, type);
            frame.setVisible(false);
            frame.dispose();
	}
	
}
