package flickr;

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JOptionPane;


/*
 *    I've created an api _key with desktop rights from this url:
 *    http://www.flickr.com/services/api/keys/apply/
 */

/**
 * Taken from http://trac.assembla.com/flickr/browser/src
 */
public class Flickr {

	private String _key;
	private String _secret;
	
	private String _frob;
	private String _token;

        private static Flickr flickr;
	
	private Flickr(String key, String secret) {
                _key = key;
                _secret = secret;

		getFrob();
		askUserAuthorisation();
		getToken();
	}

        public static Flickr getFlickr(String key, String secret) {
            if (flickr == null)
                flickr = new Flickr(key, secret);

            return flickr;
        }
	
	private void getFrob() {
		String url = "http://flickr.com/services/rest/?method=flickr.auth.getFrob&api_key="+_key;
		url = url + getSignature(url);
		String webpage = Tools.getWebpage(url);
		_frob = Tools.getXmlValue("frob", webpage);
	}
	
	private void askUserAuthorisation() {
		String url = "http://flickr.com/services/auth/?api_key="+_key+"&perms=write&frob="+_frob;
		url = url + getSignature(url);
		Tools.dispWindow("Flickr authorization", "This program requires your authorization\nbefore it can read or modify your photos and data on Flickr.\n\nYour web browser will now be opened on the flickr website to ask you for your authorization.", JOptionPane.INFORMATION_MESSAGE);
		try {
                    Desktop.getDesktop().browse(new URI(url));
		} catch (IOException e) {
                    e.printStackTrace();
		} catch (URISyntaxException e) {
                    e.printStackTrace();
		}
		Tools.dispWindow("Flickr authorization", "Click ok when you have done in your web brower.", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void getToken() {
		String url = "http://flickr.com/services/rest/?method=flickr.auth.getToken&api_key="+_key+"&frob="+_frob;
		url = url + getSignature(url);
		String webpage = Tools.getWebpage(url);
		_token = Tools.getXmlValue("token", webpage);
	}
	
	public void test() {
		String url = "http://flickr.com/services/rest/?method=flickr.blogs.getList&api_key="+_key+"&auth_token="+_token;
		url = url + getSignature(url);
		System.out.println(url);
		String webpage = Tools.getWebpage(url);		
		System.out.println(webpage);	
	}

	public String uploadPicture(String file) {
		return uploadPicture(new File(file));
	}
	
	public String uploadPicture(File file) {
		String url = "http://api.flickr.com/services/upload/?api_key="+_key+"&auth_token="+_token;
		url = url + getSignature(url);
		String webpage = Tools.postWebpage(url, "photo", file);
		String photoId = Tools.getXmlValue("photoid", webpage);
		return photoId;
	}
	
	
	private String getType(int type) {
		if (type == 0) return "Square";
		else if (type == 1) return  "Thumbnail";
		else if (type == 2) return "Small";
		else if (type == 3) return "Medium";
		else if (type == 4) return "Large";
		else return "Original";
	}
	
	
	public Dimension getPhotoSize(String photoId, int type) {
		String url = "http://flickr.com/services/rest/?method=flickr.photos.getSizes&api_key="+_key+"&auth_token="+_token+"&photo_id=" + photoId;
		url = url + getSignature(url);
		String webpage = Tools.getWebpage(url);
		String size = getType(type);
		String width = Tools.getXmlParameterWithLineContain("size", "width", size, webpage);
		String height = Tools.getXmlParameterWithLineContain("size", "width", size, webpage);
		if ((width.length() != 0) && (height.length() != 0))
			return new Dimension(Integer.parseInt(width), Integer.parseInt(height));
		return null;
	}
	
	public String getPhotoUrl(String photoId, int type) {
		String url = "http://flickr.com/services/rest/?method=flickr.photos.getSizes&api_key="+_key+"&auth_token="+_token+"&photo_id=" + photoId;
		url = url + getSignature(url);
		String webpage = Tools.getWebpage(url);
		String size = getType(type);
		return Tools.getXmlParameterWithLineContain("size", "url", size, webpage);
	}
	
	public String getPhotoStaticUrl(String photoId, int type) {
		String url = "http://flickr.com/services/rest/?method=flickr.photos.getSizes&api_key="+_key+"&auth_token="+_token+"&photo_id=" + photoId;
		url = url + getSignature(url);
		String webpage = Tools.getWebpage(url);
		String size = getType(type);
		return Tools.getXmlParameterWithLineContain("size", "source", size, webpage);
	}
	
	
	//Compute the signature of the given url 
	private String getSignature(String url) {
		//a: Retrieve the last part of the url
		int i = url.indexOf("?");
		if (i == -1) return "";
		if (i+1 == url.length()) return "";
		url = url.substring(i+1);
		//b: Retrieve each pair of "_key=value" into the url
		//(each pair is delimited with the '&' caracter)
		SortedMap<String, String> map = new TreeMap<String, String>();
		String[] keysvalues = url.split("&");
		for (int j = 0; j < keysvalues.length; j++) {
			//split each pair "_key=value" on the '=' caracter to separate them
			String[] kv = keysvalues[j].split("=");
			map.put(kv[0], kv[1]);
		}
		//c: compute the non crypted signature
		String sig = _secret;
		for (Iterator<Map.Entry<String, String>> it = map.entrySet().iterator(); it.hasNext(); ) {
	        Map.Entry<String, String> entry = it.next();
	        String key = entry.getKey();
	        String value = entry.getValue();
	        sig = sig + key + value;
		}
		//d: compute the md5 of the signature and return the result
		sig = Tools.md5(sig);
		return "&api_sig=" + sig;
	}
	

	
	public static void main(String[] args) {
		Flickr f = new Flickr("", "");
		//f.uploadPicture("C:\\Documents and Settings\\Alexandre\\Bureau\\test\\F1000011.jpg");
		Dimension d = f.getPhotoSize("2743164005", 5);
		System.out.println(d);
		System.out.println(f.getPhotoUrl("2743164005", 5));
		System.out.println(f.getPhotoStaticUrl("2743164005", 5));
	}
}
