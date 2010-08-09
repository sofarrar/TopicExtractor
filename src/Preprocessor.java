import java.io.*;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.mail.internet.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.swing.*;
import java.util.regex.*;

/**
*Class to get Chinese documents from Bing Search Engine
*/
class Preprocessor 
{
    static XPathFactory factory = null;
    static XPath xpath = null;
    static XPathExpression expr = null;

    // global constant variable 
    private static final String AppId = "4BD643DA5B78E9D533191799C97619734F41CEAE";
    private static final String requestString = "http://api.search.live.net/xml.aspx?";
    private static final String webCount = "50";


    /* *
     * main 
     * @param args ; query topic
     * @throws java.io.IOException and others
     */
     public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException,XPathExpressionException {

      // first parameter; search topic
      String queryString = args[0];

      // Build the request.
      String requestURL = BuildRequest(queryString);

      // Send the request to the Live Search Service and get the response.
      Document doc = GetResponse(requestURL);

      if(doc != null)
      {
         // Display the response obtained from the Live Search Service.
         DisplayResponse(doc);
       }
    }

    /* *
     * Build Request 
     * @param queryString
     */
    private static String BuildRequest(String queryString) {

      String request = requestString
      // Required request fields 
      + "AppId=" + AppId
      + "&Query=" + queryString
      + "&Sources=Web"

      // optional request fields
      + "&Version=2.0"
      + "&Market=zh-cn"
      + "&Adult=Moderate"

      // optional web-specific request fields
      + "&Web.Count=" + webCount
      + "&Web.Offset=0"
      //+ "&Web.FileType=DOC"
      + "&Web.Options=DisableHostCollapsing+DisableQueryAlterations";

      return request;

    }

    /* *
     * Get response 
     * @param requestURL 
     * @throws java.io.IOException and others
     */
    private static Document GetResponse(String requestURL) throws ParserConfigurationException, SAXException,IOException {

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      Document doc = null;
      DocumentBuilder db = dbf.newDocumentBuilder();

      if (db != null)
      {              
        doc = db.parse(requestURL);
      }

       return doc;
     }

    /* *
     * Display response 
     * @param doc 
     * @throws javax.xml.xpath.XPathExpressionException
     */
    private static void DisplayResponse(Document doc) throws XPathExpressionException {
      factory = XPathFactory.newInstance();
      xpath = factory.newXPath();
      xpath.setNamespaceContext(new APINameSpaceContext());
      NodeList errors = (NodeList) xpath.evaluate("//api:Error",doc,XPathConstants.NODESET);

      if(errors != null && errors.getLength() > 0 )
      {
        // There are errors in the response. Display error details.
        DisplayErrors(errors);
      }
      else
      {
        DisplayResults(doc);
      }
    }


    /* *
     * Display results 
     * @param doc 
     * @throws javax.xml.xpath.XPathExpressionException
     */
    private static void DisplayResults(Document doc) throws XPathExpressionException {

      String version = (String)xpath.evaluate("//@Version",doc,XPathConstants.STRING);
      String searchTerms = (String)xpath.evaluate("//api:SearchTerms",doc,XPathConstants.STRING);
      int total = Integer.parseInt((String)xpath.evaluate("//web:Web/web:Total",doc,XPathConstants.STRING));
      int offset = Integer.parseInt((String)xpath.evaluate("//web:Web/web:Offset",doc,XPathConstants.STRING));
      NodeList results = (NodeList)xpath.evaluate("//web:Web/web:Results/web:WebResult",doc,XPathConstants.NODESET); 

      String st  = "";
      try {
        byte[] bytes = searchTerms.getBytes("UTF-8");
        st = new String(bytes);
      } catch (Exception e) {}
      
      // Display the results header.
     System.out.println("Live Search API Version " + version);
     System.out.println("Web results for " + st);
     System.out.println("Displaying " + (offset+1) + " to " + (offset + results.getLength()) + " of " + total + " results ");
     System.out.println();

     StringBuilder builder = new StringBuilder();
     StringBuffer buf = new StringBuffer();

     for(int i = 0 ; i < results.getLength(); i++)
     {
        NodeList childNodes = results.item(i).getChildNodes();

        for (int j = 0; j < childNodes.getLength(); j++) 
        {
           if(!childNodes.item(j).getLocalName().equalsIgnoreCase("DisplayUrl"))
           {
               String fieldName = childNodes.item(j).getLocalName();
               String content = childNodes.item(j).getTextContent();

               try {
                 PrintStream out = new PrintStream("prep.txt","UTF-8");
                 System.setOut(out);
                 builder.append(fieldName + ": " + content);
                 builder.append("\n");

               } catch(Exception e) {}

               // Read content of each page
              if(fieldName.equalsIgnoreCase("url")) 
               { 
                 String urladdress = childNodes.item(j).getTextContent();

                 try
                 {
                    URL url = new URL(urladdress);
                    URLConnection connection = url.openConnection();
                    connection.setDoInput(true);

                    //set default charset of content
                    String charset = "GB2312";
                    String ct = connection.getContentType();
                    String cs = "";

                    if (ct != null) {
                      cs = new ContentType(ct).getParameter("charset");
                      if (cs != null) {
                        charset = cs;
                        builder.append("charset is: " + charset + "\n");
                      } else {
                          builder.append("unresolved charset: " + cs + "\n");
                        }
                    }

                    //regular expression to extract charset
                    Pattern pat = Pattern.compile("charset=([\\w-]+)");
                    Matcher matcher;

                    // Read contents
                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is,charset);
                    BufferedReader br = new BufferedReader(isr);
                 
                    String line = "";
                    while ((line = br.readLine()) != null)
                    {
                      // couldn't retrieve charset from content
                      if (cs == null) {
                        matcher = pat.matcher(line);
                        if (matcher.find()){
                          cs = matcher.group(1);
                          // extract charset from the data
                          // then encode the rest with this charset
                          if (!cs.equalsIgnoreCase(charset)) {
                            isr = new InputStreamReader(is,cs);
                            br = new BufferedReader(isr);
                          }
                        }
                      }
                      builder.append(line);
                    }

                    br.close();

                 } catch (Exception e) {
                   System.out.println(e.toString()); }

               }
           }
       }

//       builder.append("\n");

     }
       
       System.out.println(builder.toString());
   }


    /* *
     * Display errors 
     * @param doc 
     */
    private static void DisplayErrors(NodeList errors) 
    {
       System.out.println("Live Search API Errors:");
       System.out.println();

       for (int i = 0; i < errors.getLength(); i++) 
       {
          NodeList childNodes = errors.item(i).getChildNodes();

          for (int j = 0; j < childNodes.getLength(); j++) 
          {
             System.out.println(childNodes.item(j).getLocalName() + ":" + childNodes.item(j).getTextContent());
          }
             System.out.println();
        }
     }

    private static ByteArrayOutputStream readContents(InputStream buffer) 
    {
      final int size = 1024 * 100; // 100K
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] charbuf = new byte[size];

      try{
        int n;
        while ((n = buffer.read(charbuf,0,size)) > 0) {
          baos.write(charbuf,0,n);
        }
      } catch (IOException e) {}

      return baos;
    }

}
