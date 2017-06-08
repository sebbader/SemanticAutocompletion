package main.java.namespaces;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;

public class OA {
	
	public static final String NS = "http://www.w3.org/ns/oa#";
	
	public static final Resource Annotation = new Resource(NS + "Annotation");

	public static final Resource TextQuoteSelector = new Resource(NS + "TextQuoteSelector");

	public static final Resource SpecificResource = new Resource(NS + "SpecificResource");

	public static final Resource RangeSelector = new Resource(NS + "RangeSelector");

	public static final Resource FragmentSelector = new Resource(NS + "FragmentSelector");
	
	
	

	public static final Resource hasBody = new Resource(NS + "hasBody");

	public static final Resource hasTarget = new Resource(NS + "hasTarget");

	public static final Resource hasSelector = new Resource(NS + "hasSelector");

	public static final Resource hasEndSelector = new Resource(NS + "hasEndSelector");

	public static final Resource hasStartSelector = new Resource(NS + "hasStartSelector");

	public static final Resource refinedBy = new Resource(NS + "refinedBy");

	public static final Resource exact = new Resource(NS + "exact");

	public static final Resource hasSource = new Resource(NS + "hasSource");
	
	

	public static final Resource motivatedBy = new Resource(NS + "motivatedBy");
	
	
	

	public static final Resource classifying = new Resource(NS + "classifying");
	public static final Resource describing = new Resource(NS + "describing");


}
