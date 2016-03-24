
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 源程序来自pdfbox源码包的org.apache.pdfbox.pdmodel.CreateBookmarks
 * 程序功能：读取xml配置文件。处理由页码和名称组成的xml树
 * Remarks：如果文件只包含根节点，程序会删除所有的标签
 * 
 *@author wangzonglei
 *
 */
public final class CreateBookmarks
{
	private static PDDocument document = null;
	private static PDPageTree tree = null;
    private CreateBookmarks()
    {
        //utility class
    }

    /**
     * This will print the documents data.
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error parsing the document.
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    public static void main( String[] args ) throws IOException, SAXException, ParserConfigurationException
    {
        if( args.length != 2 )
        {
            usage();
        }
        else
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document markdoc = null;
            try
            {
                document = PDDocument.load( new File(args[1]) );
                markdoc = builder.parse(new File(args[0]));
                if( document.isEncrypted() )
                {
                    System.err.println( "Error: Cannot add bookmarks to encrypted document." );
                    System.exit( 1 );
                }
                PDDocumentOutline outline =  new PDDocumentOutline();
                document.getDocumentCatalog().setDocumentOutline( outline );
                tree = document.getPages();
                if(markdoc.hasChildNodes()){
                	Element root = markdoc.getDocumentElement(); 
                	setItems_r(root, outline);
                }
                
                /*
                pagesOutline.setTitle( "All Pages" );
                outline.addLast( pagesOutline );
                */
                outline.openNode();
                document.save( args[1] );
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
     * 遍历xml树，生成书签树
     * @param pe
     * @param pd
     */
    private static void setItems_r(Element pe, PDOutlineNode pd){
    	NodeList children = pe.getChildNodes();
    	for(int i=0;i<children.getLength();i++){
    		Node node = children.item(i);
    		if(node instanceof Element){
    			Element e = (Element) node;
    			int num = Integer.parseInt(e.getAttribute("page"));
    			String name = e.getAttribute("name");
    			// 生成标签
    			PDPage page = tree.get(num-1);
    			PDPageFitWidthDestination dest = new PDPageFitWidthDestination();
            	dest.setPage( page );
            	PDOutlineItem bookmark = new PDOutlineItem();
            	bookmark.setDestination( dest );
            	bookmark.setTitle(name);
            	pd.addLast(bookmark);
            	
            	setItems_r(e, bookmark);
    		}
    	}
    }
    
    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println( "Usage: java " + CreateBookmarks.class.getName() + " < mark-file in-out pdf>" );
    }
}
