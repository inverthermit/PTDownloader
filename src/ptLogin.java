import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import javax.security.auth.login.Configuration;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;


public class ptLogin {

	/**
	 * @param args
	 */
	public static String FILEPATH="d://torrents/";
	public static String KEYFILENAME="keywords&SE.txt";
	public static HashMap<String,String> keywordsSEList=new HashMap<String,String>();
	public static HashMap<String,String> OriginList=new HashMap<String,String>();
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build();  
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();  
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("username", "ladimeya"));
		formparams.add(new BasicNameValuePair("password", "ladimeyaladimey"));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		HttpPost httppost = new HttpPost("http://pt.hit.edu.cn/takelogin.php");
		httppost.setEntity(entity);
		httpclient.execute(httppost); 
		
		
		//http://pt.hit.edu.cn/torrents.php?cat=402
		HttpGet httpGet2 = new HttpGet("http://pt.hit.edu.cn/torrents.php?cat=402");
		HttpResponse response2 = httpclient.execute(httpGet2); 
		HttpEntity entity2 = response2.getEntity(); 
		String htmls="";
		if (entity2 != null) { 
		    htmls=EntityUtils.toString(entity2);
		    //System.out.println(htmls);
		}   
		else
		{
			System.out.println("Get list failed!");
			httpclient.close();
			return;
		}
		
		ArrayList<String> urls=getList(htmls,getkeywordsSE());	
		for(int i=0;i<urls.size();i++)
		{
			boolean temp=downloadSeed(httpclient,urls.get(i));
			if(temp==false)
				System.out.println("Download failed:"+urls.get(i));
		}
		
		//System.out.println("Got reply!");
		//htmls=HTMLFilter(htmls);
		SaveKeySE(keywordsSEList);
		httpclient.close();
	}
	
	public static ArrayList<String> getkeywordsSE()
	{
		try{
			ArrayList<String> result=new ArrayList<String>();
			BufferedReader br=new BufferedReader(new FileReader(FILEPATH+KEYFILENAME));
			while(br.ready())
			{
				String line=br.readLine();
				if(!line.equals(""))
				{
					//System.out.println(line);
					result.add(line);
				}
			
			}
			br.close();
			return result;
		}
		catch(Exception ee)
		{
			System.out.println("Get keywords failed.");
		}
		return null;
		
	}
	
	public static boolean downloadSeed(CloseableHttpClient httpclient,String url)
	{
		try{
			HttpGet httpGet = new HttpGet(url); 
			HttpResponse response = httpclient.execute(httpGet);  
			HttpEntity entity1 = response.getEntity();  
			for(int i=0;i<response.getAllHeaders().length;i++)
			{
				Header h=response.getAllHeaders()[i];
				if(h.getName().equals("Content-Disposition"))
				{
					//System.out.println(i);
					//System.out.println(URLDecoder.decode(h.getValue(), "UTF-8"));
					String fileName = URLDecoder.decode(h.getValue().substring(h.getValue().indexOf("filename=")+9),"UTF-8");
					System.out.println(fileName);
					InputStream is = entity1.getContent();  
		            File file = new File(FILEPATH+fileName);  
		            file.getParentFile().mkdirs();  
		            FileOutputStream fileout = new FileOutputStream(file);  


		            byte[] buffer=new byte[1024];
		            int ch = 0;  
		            while ((ch = is.read(buffer)) != -1) {  
		                fileout.write(buffer,0,ch);  
		            }  
		            is.close();  
		            fileout.flush();  
		            fileout.close();   
					break;
				}
				
				
			}
			
			return true;
		}
		catch(Exception ee)
		{
			return false;
		}
		
	}
	
	public static ArrayList<String> getList(String htmls,ArrayList<String> key) throws Exception{
		ArrayList<String> result=new ArrayList<String>();
		Parser	parser=Parser.createParser(htmls, "utf-8");
   	    AndFilter ProfessionNameFilter=new AndFilter(new TagNameFilter("a"),
                   new HasAttributeFilter("target","_blank"));
   	    NodeList nodes4=parser.extractAllNodesThatMatch(ProfessionNameFilter);
   	    for(int i=0;i<key.size();i++)
		{
   	    	String[] temp=key.get(i).split("###");
			if(temp.length==2)
			{
				keywordsSEList.put(temp[0], temp[1]);
			}
		}
   	    for(int i=0;i<nodes4.size();i++)
   	    {
   	    	LinkTag link=(LinkTag)nodes4.elementAt(i);
   	    	if(!link.getAttribute("href").equals("#"))
   	    	{
   	    		String content=html2Str(link.toHtml()).toLowerCase();
   	    		String SE=getSE(html2Str(link.toHtml()));
   	    		if(SE!=null)
   	    		{
   	    			for(int j=0;j<key.size();j++)
   	    			{
   	    				String[] temp=key.get(j).split("###");
   	    				if(temp.length==2)
   	    				{
   	    					
   	    					String[] keys=temp[0].split(";");
   	    					if(keys.length>0)
   	    					{
   	    						boolean isListed=true;
   	   	    					for(String a:keys)
   	   	    					{
   	   	    						if(!content.contains(a))
   	   	    							isListed=false;
   	   	    						
   	   	    					}
   	   	    					if(isListed)
   	   	   	   	    			{
   	   	   	   	    				if(isEpAfter(SE,temp[1]))
   	   	   	   	   	    			{
   	   	   	   	   	    				System.out.println(html2Str(link.toHtml()));
   	   	   	   	   	    				
   	   	   	   	   	    			    String id=getSeedID(link.getAttribute("href"));
   	   	   	   	   	    			    if(id!=null)
   	   	   	   	   	    			    {
   	   	   	   	   	    			    	System.out.println("http://pt.hit.edu.cn/download.php?id="+id);
   	   	   	   	   	    			    	//System.out.println(SE);
   	   	   	   	   	    			    	if(isEpAfter(SE,keywordsSEList.get(temp[0])))
   	   	   	   	   	    			    	keywordsSEList.put(temp[0], SE);
   	   	   	   	   	    			    	/*
					   	   	   	   	   	    Iterator iter=keywordsSEList.entrySet().iterator();
					   	   	   				while (iter.hasNext()) {
					   	   	   				Map.Entry entry = (Map.Entry) iter.next();
					   	   	   				String key1 = (String)entry.getKey();
					   	   	   				String val = (String)entry.getValue();
					   	   	   				String line=key1+"###"+val+"\n";
					   	   	   				System.out.print(line);
					   	   	   				}*/
   	   	   	   	   	    			    	//System.out.println(keywordsSEList.get(temp[0]));
   	   	   	   	   	    			    	result.add("http://pt.hit.edu.cn/download.php?id="+id);
   	   	   	   	   	    			    }
   	   	   	   	   	    			    else
   	   	   	   	   	    			    {
   	   	   	   	   	    			    	System.out.println("Couldn't get id. Seed failed.");
   	   	   	   	   	    			    }
   	   	   	   	   	    			    
   	   	   	   	   	    			}
   	   	   	   	    			}
   	    					}
   	    					
   	    				}
   	    				
   	    			}
   	    			
   	    		}
   	    		
   	    		
   	    	}
   	    	
   	    }
   	
		return result;
	}

	public static boolean SaveKeySE(HashMap<String,String> map) throws Exception
	{
		StringBuffer sb=new StringBuffer();
		Iterator iter=map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String)entry.getKey();
			String val = (String)entry.getValue();
			String line=key+"###"+val+"\n";
			//System.out.print(line);
			sb.append(line);
			}
		FileOutputStream fo=new FileOutputStream(FILEPATH+KEYFILENAME);
		fo.write((new String(sb)).getBytes());
		fo.close();
		return true;
	}
	
	public static String html2Str(String html) { 
		return html.replaceAll("<[^>]+>", "");
	}
	
	public static String getSE(String title)
	{
		Pattern p = Pattern.compile("S[0-9]{2}E[0-9]{2}");
    	Matcher m = p.matcher(title);
    	if (m.find()) 
    	{
    		String result=m.group();
    	 	return result;
    	 }
    	return null;
    	 
	}
	
	public static String getSeedID(String href)
	{
		Pattern p = Pattern.compile("id=[0-9]+");
    	Matcher m = p.matcher(href);
    	if (m.find()) 
    	{
    		String result=m.group();
    	 	return result.replace("id=", "");
    	 }
    	return null;
	}
	public static String getRootPath(){
		String path = "";
		try{
			path = java.net.URLDecoder.decode(new ptLogin().getClass().getProtectionDomain().getCodeSource().getLocation().getPath(),"UTF-8");
			path = path.substring(0, path.lastIndexOf(File.separator));
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return path;
	}
	public static boolean isEpAfter(String curEp,String lastEp)//S09E12
	{
		String Lseason=lastEp.split("E")[0].replace("S0", "").replace("S", "");
		String Lepisode=lastEp.split("E")[1].replace("E0", "").replace("E", "");
		String Cseason=curEp.split("E")[0].replace("S0", "").replace("S", "");
		String Cepisode=curEp.split("E")[1].replace("E0", "").replace("E", "");
		if(Integer.parseInt(Lseason)<Integer.parseInt(Cseason))
		{
			return true;
		}
		else
		{
			if(Integer.parseInt(Lepisode)<Integer.parseInt(Cepisode))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}
}
