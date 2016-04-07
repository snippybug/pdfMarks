import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 基于pdfbox的实例代码org.apache.pdfbox.examples.pdmodel.PrintBookmarks;
 * 程序会读取pdf文档内的标签树，然后生成CreateBookmarks能阅读的xml树
 * 
 * @author wangzonglei
 *
 */
public final class ReadBookmarks {
	private static Document xmldoc = null;
    private ReadBookmarks()
    {
        //utility class
    }
    
    public static void main( String[] args ) throws IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException
    {
        if( args.length != 2 )
        {
            usage();
        }
        else
        {
            PDDocument document = null;
            
            try
            {
                document = PDDocument.load( new File(args[0]) );
                ReadBookmarks meta = new ReadBookmarks();
                PDDocumentOutline outline =  document.getDocumentCatalog().getDocumentOutline();
                if( outline != null )
                {
                	xmldoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                	Element root = xmldoc.createElement("document");
                	String name;
                	int index;
                	if((index=args[0].lastIndexOf('/')) == -1){
                		name = args[0];
                	}else{
                		name = args[0].substring(index+1);
                	}
                	root.setAttribute("name", name);
                	xmldoc.appendChild(root);
                	
                	// 递归添加标签
                    meta.readBookmark(outline, root);
                    
                    Transformer t = TransformerFactory.newInstance().newTransformer();
                    // 设置缩进
            		t.setOutputProperty(OutputKeys.INDENT, "yes");
            		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            		// 写出
            		t.transform(new DOMSource(xmldoc), new StreamResult(new File(args[1])));
                }
                else
                {
                    System.out.println( "This document does not contain any bookmarks" );
                }
            }
            finally
            {
                if( document != null )
                {
                    document.close();
                }
            }
        }
    }

    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println( "Usage: java " + ReadBookmarks.class.getName() + " <input-pdf mark-file>" );
    }

    public void readBookmark( PDOutlineNode pmark, Element pxml ) throws IOException
    {
        for(PDOutlineItem child : pmark.children()){
        	String name = child.getTitle();
        	PDDestination dest = child.getDestination();
        	Integer page = -1;
        	if(dest instanceof PDPageDestination){
        		page = ((PDPageDestination)dest).retrievePageNumber();
        		// 创建新结点
        		Element e = xmldoc.createElement("mark");
        		e.setAttribute("name", name);
        		e.setAttribute("page", ((Integer)(page+1)).toString());			// pdf文档的页码从0计数，但用户读取时从1计数
        		// 放入父节点
        		pxml.appendChild(e);
        		// 递归处理
        		readBookmark(child, e);
        	}
        }
    }
}
