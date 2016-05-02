package org.mdp.cli; 
 
import java.io.BufferedReader; 
import java.io.File; 
import java.io.IOException; 
import java.io.InputStreamReader; 
import java.rmi.AlreadyBoundException; 
import java.util.HashMap; 
 
import org.apache.commons.cli.BasicParser; 
import org.apache.commons.cli.CommandLine; 
import org.apache.commons.cli.CommandLineParser; 
import org.apache.commons.cli.HelpFormatter; 
import org.apache.commons.cli.Option; 
import org.apache.commons.cli.Options; 
import org.apache.commons.cli.ParseException; 
import org.apache.lucene.analysis.Analyzer; 
import org.apache.lucene.analysis.es.SpanishAnalyzer; 
import org.apache.lucene.document.Document; 
import org.apache.lucene.index.DirectoryReader; 
import org.apache.lucene.index.IndexReader; 
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser; 
import org.apache.lucene.search.IndexSearcher; 
import org.apache.lucene.search.Query; 
import org.apache.lucene.search.ScoreDoc; 
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version; 
import org.mdp.cli.BuildWikiIndex.FieldNames; 
 
/** 
 * Main method to search articles using Lucene. 
 *  
 * @author Aidan 
 */ 
public class SearchWikiIndex { 
 
    public static final HashMap<String,Float> BOOSTS = new HashMap<String,Float>(); 
    static { 
        BOOSTS.put(FieldNames.ABSTRACT.name(), 1f); //<- default 
        BOOSTS.put(FieldNames.TITLE.name(), 5f);  
    } 
 
    public static final int DOCS_PER_PAGE  = 10; 
 
    public static void main(String args[]) throws IOException, ClassNotFoundException, AlreadyBoundException, InstantiationException, IllegalAccessException{ 
        Option inO = new Option("i", "input index directory"); 
        inO.setArgs(1); 
        inO.setRequired(true); 
 
        Options options = new Options(); 
        options.addOption(inO); 
 
        CommandLineParser parser = new BasicParser(); 
        CommandLine cmd = null; 
 
        try { 
            cmd = parser.parse(options, args); 
        } catch (ParseException e) { 
            System.err.println("***ERROR: " + e.getClass() + ": " + e.getMessage()); 
            HelpFormatter formatter = new HelpFormatter(); 
            formatter.printHelp("parameters:", options ); 
            return; 
        } 
 
        // print help options and return 
        if (cmd.hasOption("h")) { 
            HelpFormatter formatter = new HelpFormatter(); 
            formatter.printHelp("parameters:", options ); 
            return; 
        } 
 
        String in = cmd.getOptionValue(inO.getOpt()); 
        System.err.println("Opening directory at  "+in); 
 
        startSearchApp(in); 
    } 
 
    /** 
     *  
     * @param in : the location of the index directory 
     * @throws IOException 
     */ 
    public static void startSearchApp(String in) throws IOException { 
       
    	// TODO open a reader for the directory 
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(in)));  
 

        // SpellCheck Index 
        Dictionary dictionary = new LuceneDictionary(reader, FieldNames.ABSTRACT.name());
        Directory spellIndexDirectory = new NIOFSDirectory(new File("./spellindex"));	
		SpellChecker spellChecker = new SpellChecker(spellIndexDirectory);
        
        
        // TODO open a searcher over the reader 
        IndexSearcher searcher = new IndexSearcher(reader); 
         
        // TODO use the same analyser as the build 
        Analyzer analyser = new SpanishAnalyzer(Version.LUCENE_48); 
         
        // TODO create a multi-field query parser for title and abstract 
        // set BOOSTS values above 
        MultiFieldQueryParser queryParser =  
                new MultiFieldQueryParser(Version.LUCENE_48, 
                        new String[] {FieldNames.ABSTRACT.name(), FieldNames.TITLE.name() }, 
                        analyser, 
                        SearchWikiIndex.BOOSTS); 
 
        // opens utf-8 stream from command line 
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "utf-8")); 
 
        while (true) { 
            System.out.println("Enter a keyword search phrase:"); 
 
            String line = br.readLine(); 
            if(line!=null){ 
                line = line.trim(); 
                if(!line.isEmpty()){ 
                    try{ 
                        // TODO parse query 
                        Query query = queryParser.parse(line); 
                        // TODO print raw query and parsed query object 
                        System.out.println(query.toString()); 
                        // TODO get DOCS_PER_PAGE hits  
                        TopDocs docs = searcher.search(query, SearchWikiIndex.DOCS_PER_PAGE); 
                         
                        ScoreDoc[] hits = docs.scoreDocs; 
                         
                        // TODO print number of matching documents 
                        System.out.println("Documentos encontrados: " + docs.totalHits ); 
                         
                        if ( docs.totalHits == 0 )
                        {
                        	String[] suggestEordds = spellChecker.suggestSimilar(line, 5);
                        	System.out.println(":( No se encontraron documentos ");
                        	 System.out.print("Talvez querías decir: ");
                        	for (int i = 0; i < suggestEordds.length; i++) {
								System.out.print(suggestEordds[i] +", ");
							}
                        	System.out.println();
                        }
                        
                        
                        // TODO for each hit, get its details and print them (title, abstract, etc.) 
                        System.out.println("*********************************************");     
                        for (int i = 0; i < hits.length; i++) { 
                             
                            Document doc = searcher.doc(hits[i].doc); 
                            System.out.println("Score: " + hits[i].score); 
                            System.out.println("URL: " + doc.get( FieldNames.URL.name())); 
                            System.out.println("Titulo: " + doc.get( FieldNames.TITLE.name())); 
                            System.out.println("Abstract: " + doc.get( FieldNames.ABSTRACT.name())); 
                             
                            System.out.println("_______________________________________________________");     
 
                        } 
                        System.out.println("*********************************************");     
                         
                    } catch(Exception e){ 
                        System.err.println("Error with query '"+line+"'"); 
                        e.printStackTrace(); 
                    } 
                } 
            } 
 
        } 
    } 
}