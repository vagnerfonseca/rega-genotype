package rega.genotype.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

import rega.genotype.ui.framework.exeptions.RegaGenotypeExeption;
import rega.genotype.utils.Settings;
import eu.webtoolkit.jwt.utils.StreamUtils;

/**
 * Centralize all requests to ToolRepoService.
 * @author michael
 */
public class ToolRepoServiceRequests {

	public static class ToolRepoServiceExeption extends Exception{
		private static final long serialVersionUID = -7810905835977427799L;
		public ToolRepoServiceExeption(String msg) {
			super(msg);
		}
	}

	// URLS
	
	private static String gerRepoServiceUrl() {
		return Settings.getInstance().getConfig().getGeneralConfig().getRepoUrl();
	}

	private static String getReqPublishUrl() {
		return gerRepoServiceUrl() 
				+ File.separator  +  ToolRepoService.REQ_TYPE_PUBLISH;
	}

	private static String getReqManifestsUrl() {
		return gerRepoServiceUrl() 
				+ File.separator  +  ToolRepoService.REQ_TYPE_GET_MANIFESTS;
	}

	private static String getReqToolUrl(String toolId, String toolVersion) {
		return gerRepoServiceUrl() 
				+ File.separator  +  ToolRepoService.REQ_TYPE_GET_TOOL 
				+ "?" + ToolRepoService.TOOL_ID_PARAM + "=" + toolId
				+ "&" + ToolRepoService.TOOL_VERSION_PARAM + "=" + toolVersion;
	}
	
	private static String generatePasswiord() {
		return Settings.getInstance().getConfig().getGeneralConfig().getPublisherPassword();
	}

	// requests 
	
	public static void publish(final File zipFile) throws RegaGenotypeExeption, IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(
				getReqPublishUrl()).openConnection();
		connection.setDoOutput(true); // Triggers POST.
		//connection.setRequestProperty("Accept-Charset", "UTF-8");
		//connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + "UTF-8");
		connection.setRequestProperty("Content-Type", "multipart/form-data"); // Allow to add a file
		connection.setRequestProperty(ToolRepoService.TOOL_PWD_PARAM, generatePasswiord());
		StreamUtils.copy(new FileInputStream(zipFile), 
				connection.getOutputStream());

		if (connection.getResponseCode() != 200) {
			String responseMessage = connection.getHeaderField(ToolRepoService.RESPONCE_ERRORS);
			throw new RegaGenotypeExeption(responseMessage);
		}
		// Note: can throw java.io.FileNotFoundException if the server did not respond.
		InputStream response = connection.getInputStream();

		BufferedReader in = new BufferedReader(new InputStreamReader(response));
		String decodedString;
		while ((decodedString = in.readLine()) != null) {
			System.out.println(decodedString);
		}
		in.close();
	}

	/**
	 * Requests the server for the manifests of all published tools.
	 * 
	 * @return manifests json string or null if did not work.
	 */
	public static String getManifests() {
		// TODO: maybe some users are interested only in current versions on a tool?
		URLConnection connection;
		String ans = null;
		try {
			connection = new URL(getReqManifestsUrl()).openConnection();
			//connection.setDoOutput(true); // Triggers POST.
			connection.setRequestProperty("Content-Type", "multipart/form-data"); // Allow to add a file
			connection.setRequestProperty(ToolRepoService.TOOL_PWD_PARAM, generatePasswiord());

			ans = IOUtils.toString(connection.getInputStream()); 

		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return ans;
	}

	/**
	 * Ask the service for a specific tool.
	 * @param toolId
	 * @param toolVersion
	 * @return temp zip file with resived data
	 * @throws ToolRepoServiceExeption
	 * @throws IOException
	 */
	public static File getTool(String toolId, String toolVersion) throws ToolRepoServiceExeption, IOException {
		// TODO: add to config .. 
		File ans = new File(Settings.getInstance().getXmlDir(toolId, toolVersion));
		if (ans.exists()) {
			throw new ToolRepoServiceExeption("Tool: " + toolId + " version: " + toolVersion + " exists on local server.");
		} 
		URLConnection connection;

		connection = new URL(getReqToolUrl(toolId, toolVersion)).openConnection();
		connection.setDoOutput(true); // Triggers POST.
		connection.setRequestProperty("Content-Type", "multipart/form-data"); // Allow to add a file
		connection.setRequestProperty(ToolRepoService.TOOL_PWD_PARAM, generatePasswiord());
	
		// Note: can throw java.io.FileNotFoundException if the server did not respond.
		InputStream response = connection.getInputStream();
		
		ans = File.createTempFile("tool", ".zip");

		FileOutputStream out = new FileOutputStream(ans);
		StreamUtils.copy(response, out);
		out.close();

		return ans;
	}

}
