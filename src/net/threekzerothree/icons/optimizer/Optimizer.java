package net.threekzerothree.icons.optimizer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class Optimizer {

	//
	private final static String IN_FILE_FILTERS = "res/input/xml/appfilter.xml";

	//
	private final static String IN_FILE_DRAWABLES = "res/input/drawables";

	//
	private final static String OUT_FILE_FILTERS = "res/output/xml/appfilter.xml";

	//
	private final static String OUT_FILE_DRAWABLES = "res/output/xml/drawable.xml";

	//
	private final static String OUT_FILE_ICON_PACK = "res/output/xml/icon_pack.xml";

	//
	private final static String OUT_DIR_DRAWABLES_GOLD = "res/output/drawables/gold/";

	//
	private final static String OUT_DIR_DRAWABLES_XTRA = "res/output/drawables/xtra/";

	//
	private static List<Component> mComponents = new ArrayList<>();

	//
	private static List<String> mComponentDrawables = new ArrayList<>();

	//
	private static List<String> mResourceDrawables = new ArrayList<>();

	//
	private static List<String> mUnUtilizedDrawables = new ArrayList<>();

	/**
	 *
	 * @param argv
	 */
	public static void main(String argv[]) {

		loadComponents();
		writeOptimizedComponents();
		
		loadDrawables();
		writeOptimizedDrawables();
	}

	/**
	 *
	 */
	private static void loadComponents() {
		
		// clear existing components before adding more
		mComponents.clear();
					
        try {
        	File fXmlFile = new File(IN_FILE_FILTERS);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			
			doc.getDocumentElement().normalize();
            
            NodeList nList = doc.getElementsByTagName("item");
            
            for (int temp = 0; temp < nList.getLength(); temp++) {
            	
            	Node node = nList.item(temp);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					
					Element element = (Element) node;
					String info = element.getAttribute("component");
					String drawable = element.getAttribute("drawable");
					String type = element.getAttribute("type");
					String category = element.getAttribute("category");
					
					if(type.equals("")) { // if no type, assign type based upon filename prefix
						int endSubstring = drawable.indexOf("_");
						endSubstring = endSubstring == -1 ? 0 : endSubstring;
						
						type = drawable.substring(0,endSubstring).toUpperCase();
					}
					
					Component component = new Component();
					component.setType(type);
					component.setCategory(category);
					component.setComponent(info.substring(14, info.length() - 1));
					component.setDrawable(drawable);
					
					boolean exist = false;
					
					for(Component c : mComponents) { // remove dups based upon component string value
						if(c.component.equals(component.component)) {
							exist = true;
							break;
						}
					}
					
					if(!exist)
						mComponents.add(component);
				}
			}
            
        } catch (Exception e) {
			e.printStackTrace();
		}
        
        // before we are done, sorts components.... 
		Collections.sort(mComponents, new Comparator<Component>() {
	        @Override
	        public int compare(Component o1, Component o2) {
	            int value1 = o1.type.compareTo(o2.type);
	            
	            if (value1 == 0) {
	            	return o1.component.compareTo(o2.component);
	            }
	            
	            return value1;
	        }
	    });
    }
	
	/*
	private static void optimizeComponents() {
		// sorts components.... 
		Collections.sort(mComponents, new Comparator<Component>() {
	        @Override
	        public int compare(Component o1, Component o2) {
	            int value1 = o1.type.compareTo(o2.type);
	            
	            if (value1 == 0) {
	            	return o1.component.compareTo(o2.component);
	            }
	            
	            return value1;
	        }
	    });
	}
	*/

	/**
	 *
	 */
	private static void writeOptimizedComponents() {
		
		Writer writer = null;

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUT_FILE_FILTERS), "utf-8"));
		    
		    String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		    header += "\n" + "<resources>";
		    header += "\n" + "\t<!-- Icon Layers -->";
		    header += "\n" + "\t<iconback img1=\"app_3k03_icon_background\"/>";
		    header += "\n" + "\t<iconupon img1=\"app_3k03_icon_overlay\"/>";
		    header += "\n" + "\t<iconmask img1=\"app_3k03_icon_mask\"/>";
		    header += "\n" + "\t<scale factor=\"0.8\" />";
		    
		    writer.write(header);
		    
		    String type = "";
		    for(Component component : mComponents) {
		    	String item = "\n";

				/*
		    	if(type.equals("") || !type.equals(component.type)) {
		    		type = component.type;
		    		item += "\n" + "\t<!-- " + type + " -->" + "\n";
		    	}
				*/

				item += "\t<item";
				item += " component=\"ComponentInfo{" + component.component + "}\"";
				item += " drawable=\"" + component.drawable + "\"";
				//item += " type=\"" + component.type + "\"";
				//item += " category=\"" + component.category.replace("&", "&amp;") + "\"";
				item += "/>";

				writer.write(item);
			}
		        
		    String footer = "\n" + "</resources>";
		    writer.write(footer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	private static void loadDrawables() {
		
		// load drawables from components
		// utilized within appfilter.xml
		for(Component component : mComponents) {
			mComponentDrawables.add(component.getDrawable());
		}
		
		HashSet<String> drawablesHS = new HashSet<String>();
		drawablesHS.addAll(mComponentDrawables);
		mComponentDrawables.clear();
		mComponentDrawables.addAll(drawablesHS);
		
		Collections.sort(mComponentDrawables); // sort string collection
		
		// load drawables from resource image files
		File directory = new File(IN_FILE_DRAWABLES);
		Collection<File> files = FileUtils.listFiles(
				directory,
				new RegexFileFilter("^(.*?)"),
				DirectoryFileFilter.DIRECTORY);
		
		
		String filename;
		for(File file : files) {
			filename = file.getName();
			
			int endSubstring = filename.indexOf(".");
			endSubstring = endSubstring == -1 ? 0 : endSubstring;
			
			filename = filename.substring(0,endSubstring);
			
			mResourceDrawables.add(filename);
			
			if(mComponentDrawables.contains(filename)) {
				try { // this file is used in appfilter... move to gold
					file.renameTo(new File(OUT_DIR_DRAWABLES_GOLD + file.getName()));
		    	} catch(Exception e) {
		    		// add to missing files....
		    		e.printStackTrace();
		    	}
			} else {
				try { // this file is NOT used in appfilter... move to xtra
					file.renameTo(new File(OUT_DIR_DRAWABLES_XTRA + file.getName()));
		    	} catch(Exception e) {
		    		// add to missing files....
		    		e.printStackTrace();
		    	}
			}
		}
		
		// identify un-utilized drawables
		for(String drawable : mResourceDrawables) {
			if(!mComponentDrawables.contains(drawable)) {
				mUnUtilizedDrawables.add(drawable);
				//System.out.println("NOT-UTILIZED::" + drawable);
			}
		}
		
		System.out.println("RESOURCES::TOTAL::COUNT::" + mResourceDrawables.size());
		System.out.println("RESOURCES::NOT-USED::COUNT::" + mUnUtilizedDrawables.size() + " <- SHOULD BE ZERO");
	}
	
	/**
	 * Generate drawables.xml
	 * This should be sorted by category
	 */
	private static void writeOptimizedDrawables() {
		
		// mComponents: defined image files in appfilter.xml
		// mResourceDrawables: all image files
		// mUnUtilizedDrawables: all unused image files (not used within appfilter.xml)
		
		ArrayList<Drawable> drawables = new ArrayList<>();
		
		for(Component component : mComponents) {
			
			Drawable drawable = new Drawable();
			drawable.setType(component.type);
			drawable.setCategory(component.category);
			drawable.setDrawable(component.drawable);
			
			boolean exist = false;
			
			for(Drawable d : drawables) {
				if(d.type.equals(drawable.type)
						&& d.drawable.equals(drawable.drawable)) {
					exist = true;
					break;
				}
			}
			
			if(!exist)
				drawables.add(drawable);
		}
		
		System.out.println("COMPONENTS::SIZE::TOTAL::" + mComponents.size());
		System.out.println("DRAWABLES::SIZE::NO-DUPS::" + drawables.size());
		
		// sorts drawables.... 
		Collections.sort(drawables, new Comparator<Drawable>() {
	        @Override
	        public int compare(Drawable o1, Drawable o2) {
	            int value1 = o1.type.compareTo(o2.type);
	            
	            if (value1 == 0) {
	            	return o1.drawable.compareTo(o2.drawable);
	            }
	            
	            return value1;
	        }
	    });

		Writer writer = null;

		try { // write drawable.xml
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(OUT_FILE_DRAWABLES), "utf-8"));
		    
		    String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		    header += "\n" + "<resources>";
		    header += "\n" + "	<version>1.0</version>" + "\n";
		    
		    writer.write(header);

			// add new drawables
			if(mResourceDrawables.size() > 0) {

				// sort the drawable names
				Collections.sort(mResourceDrawables);

				writer.write("	<category title=\"Recently Added\"/>" + "\n");

				for (String drawable : mResourceDrawables) {
					writer.write("	<item drawable=\"" + drawable + "\"/>" + "\n");
				}
			}

			// add all other drawables (unique only)
		    String category = "";
		    
		    List<String> uniques = new ArrayList<>();
		    
		    for(Drawable drawable : drawables) {
		    	String item = "";
		    	
		    	if(!drawable.type.equals("3K03")) {
			    	if(!category.equals(drawable.category)) {
			    		category = drawable.category;
						item += "	<category title=\"" + drawable.category.replace("&", "&amp;") + "\"/>" + "\n";
			    	}
			    	
			    	if(!uniques.contains(drawable.drawable)
			    			|| drawable.type.equals("X_BRAND")
			    			|| drawable.type.equals("Y_SYS")) {
			    		
			    		item += "	<item drawable=\"";
						item += drawable.drawable;
						item += "\"/>" + "\n";
						
						if(!uniques.contains(drawable.drawable)) {
							uniques.add(drawable.drawable);
						}
			    	}

					writer.write(item);
		    	}
			}
		    

			/*
			// sort unused for later...
			Collections.sort(mUnUtilizedDrawables);

		    // drawables not in appfilter
		    writer.write("	<category title=\"Extras\"/>" + "\n");
		    
		    for(String drawable : mUnUtilizedDrawables) { 
		    	
		    	String item = "";
		    	item += "	<item drawable=\"";
				item += drawable;
				item += "\"/>" + "\n";
					
				writer.write(item);
			}
		    */

		    writer.write("</resources>");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try { // write icon_pack.xml
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(OUT_FILE_ICON_PACK), "utf-8"));
		    
		    String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		    header += "\n" + "<resources>";
		    header += "\n" + "\t<string-array name=\"icon_pack\" translatable=\"false\">";
		    
		    writer.write(header);
		    
		    List<String> uniques = new ArrayList<String>();
		    
		    for(Drawable drawable : drawables) {
		    	if(!drawable.type.equals("3K03")) {

		    		if(!uniques.contains(drawable.drawable)
			    			|| drawable.type.equals("X_BRAND")
			    			|| drawable.type.equals("Y_SYS")) {
		    			
		    			String item = "\n" + "		<item>" + drawable.drawable + "</item>";
		    			writer.write(item);
		    			
		    			if(!uniques.contains(drawable.drawable))
		    				uniques.add(drawable.drawable);
			    	}
		    	}
			}
		    
		    // drawables not in appfilter
		    for(String drawable : mUnUtilizedDrawables) { 
		    	String item = "\n" + "		<item>" + drawable + "</item>";
				writer.write(item);
			}
		    
		    String footer = "\n" + "\t</string-array>";
		    footer += "\n" + "</resources>";
		    
		    writer.write(footer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	private static void moveFile(File file, String dir) {
		
		try {
			file.renameTo(new File(dir + "/" + file.getName()));

    	} catch(Exception e) {
    		e.printStackTrace();
    	}
	}
	*/
}
