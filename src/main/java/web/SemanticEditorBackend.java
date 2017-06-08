package main.java.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.ClientProtocolException;
import org.apache.jena.vocabulary.DCTerms;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.JerseyInvocation;
import org.json.*;
import org.semanticweb.yars.jaxrs.JsonLdMessageBodyReaderWriter;
import org.semanticweb.yars.jaxrs.NxMessageBodyReaderWriter;
import org.semanticweb.yars.jaxrs.RdfXmlMessageBodyWriter;
import org.semanticweb.yars.jaxrs.TurtleMessageBodyReader;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.dt.string.XsdLanguage;
import org.semanticweb.yars.nx.dt.string.XsdString;
import org.semanticweb.yars.nx.namespace.DCTERMS;
import org.semanticweb.yars.nx.namespace.LDP;
import org.semanticweb.yars.nx.namespace.OWL;
import org.semanticweb.yars.nx.namespace.RDF;
import org.semanticweb.yars.nx.namespace.RDFS;
import org.semanticweb.yars.nx.namespace.XSD;
import org.semanticweb.yars.util.ConcatenatingIterable;

import main.java.lucene.search.LuceneSearcher;
import main.java.namespaces.IIRDS;
import main.java.namespaces.OA;
import main.java.namespaces.STEP;


/**
 * 
 * @author sba
 *
 */
@Path("/editorbackend")
public class SemanticEditorBackend {
	@Context
	ServletContext _ctx;

	public final String indexLocation = "/resources/StepOntologyIndex/lucene";

	private final String db_user = "admin";
	private final String db_pwd = "pass123";


	@GET
	public Response getOverview(@Context UriInfo uriinfo, @Context Request request) {

		return Response.ok().build();
	}



	/*
	 * TODO delete method ?
	 * 
	@Path("new-jsonld")
	@POST
	public Response postNewJsonLd(@Context UriInfo uriinfo, String input, @Context HttpHeaders headers) throws ClientProtocolException, IOException 
	{
		JSONObject jsonObj = new JSONObject(input);
		String newJSONString = jsonObj.get("this").toString();

		try
		{
			jsonObj = new JSONObject(newJSONString.substring(1, newJSONString.length()-1));
		}
		catch (Exception ex)
		{

		}

		//do something useful with new JSON Object (actually JSON-LD Object) here
		//
		//

		String answer = "{\"result\":[\"one\", \"two\", \"three\", \"four\", \"five\"]}";
		JSONObject newJsonObj = new JSONObject(answer);

		return Response.ok(newJsonObj.toString()).build();
	}
	 */


	@Path("/savereport")
	@POST
	//@Consumes(JsonLdMessageBodyReaderWriter.JSONLD_MEDIATYPE.getType())
	public Response saveReport(@Context UriInfo uriinfo, String input, @Context HttpHeaders headers) throws ClientProtocolException, IOException {
		JSONObject jsonObj = new JSONObject(input);
		List<Node[]> listOfNodes = new ArrayList<Node[]>();

		// Base Report information
		Node base = new Resource("#this");
		listOfNodes.add(new Node[] { base,		RDF.TYPE,										OA.Annotation });
		String date = (new SimpleDateFormat("YYYY-MM-dd hh:mm:ss")).format(Calendar.getInstance().getTime());
		listOfNodes.add(new Node[] { base, 		DCTERMS.CREATED, 								new Literal( date , XSD.STRING) });// Date TODO
		listOfNodes.add(new Node[] { base, 		new Resource( DCTerms.creator.getURI() ),  		new Literal(jsonObj.getString("Name")) });
		listOfNodes.add(new Node[] { base, 		new Resource( DCTerms.hasVersion.getURI() ),  	new Literal(jsonObj.getString("Einsatz")) });
		listOfNodes.add(new Node[] { base, 		OA.motivatedBy,  								OA.describing });
		listOfNodes.add(new Node[] { base,		new Resource("http://schema.org/customer"),		new Literal(jsonObj.getString("Kunde")) });
		listOfNodes.add(new Node[] { base,		OA.hasBody,										new Literal(jsonObj.getString("Kunde")) });
		listOfNodes.add(new Node[] { base,		OA.hasBody,										new Literal(jsonObj.getString("Maschinentyp")) });
		listOfNodes.add(new Node[] { base,		OA.hasBody,										new Literal(jsonObj.getString("Maschinen-ID")) });
		listOfNodes.add(new Node[] { base,		OA.hasBody,										new Literal(jsonObj.getString("Fehlercode")) });
		listOfNodes.add(new Node[] { base,		OA.hasBody,										new Literal(jsonObj.getString("Komponente")) });
		listOfNodes.add(new Node[] { base,		new Resource("http://schema.org/duration"),		new Literal(jsonObj.getString("Time")) });
		listOfNodes.add(new Node[] { base,		new Resource("http://example.org/isRealOrPlacebo"),new Literal(jsonObj.getString("Evaluation")) });
		for (int i = 0; i < jsonObj.getJSONArray("Classes").length(); i++ ) {
			if (jsonObj.getJSONArray("Classes").isNull(0)) break;

			try {
				JSONArray array = jsonObj.getJSONArray("Classes").getJSONArray(i);
				for (int j = 0; j < array.length(); j++ ) {
					// new Annotation
					String c = (String) array.get(j) ;
					listOfNodes.add(
							new Node[] { base,		OA.hasBody,										new Resource(c) });					
				}
			} catch (JSONException e) {/**/}




			try {
				// new Annotation
				String c = (String) jsonObj.getJSONArray("Classes").get(i) ;
				if (c.contains(",")) {
					String[] array = c.split(",");
					for (int j = 0; j < array.length; j++ ) {
						// new Annotation
						String s = (String) array[j] ;
						listOfNodes.add(
								new Node[] { base,		OA.hasBody,										new Resource(s) });					
					}
				} else {
					listOfNodes.add(
							new Node[] { base,		OA.hasBody,										new Resource(c) });
				}
			} catch (JSONException e) {/**/}

		}


		// Target
		Node target = new Resource("#target");
		listOfNodes.add(new Node[] { base,		OA.hasTarget,								target});
		listOfNodes.add(new Node[] { target,	RDF.TYPE,									OA.SpecificResource });		
		listOfNodes.add(new Node[] { target,	new Resource( DCTerms.format.getURI() ),	new Literal("text/plain", XSD.STRING ) });	


		// textquoteselector
		Node textquoteselector = new Resource("#textquoteselector");
		listOfNodes.add(new Node[] { target,				OA.hasSelector,		textquoteselector });
		listOfNodes.add(new Node[] { textquoteselector,		RDF.TYPE,			OA.TextQuoteSelector} );
		listOfNodes.add(new Node[] { textquoteselector,		RDF.VALUE,			new Literal( jsonObj.getString("Einsatzbericht"), XSD.STRING) } );


		/*
		 * build Client
		 */
		Client client = ClientBuilder.newBuilder()
				.register(JsonLdMessageBodyReaderWriter.class)
				.register(TurtleMessageBodyReader.class)
				.register(RdfXmlMessageBodyWriter.class)
				.build();

		JerseyInvocation.Builder b = (org.glassfish.jersey.client.JerseyInvocation.Builder) client
				.target("http://km.aifb.kit.edu/services/marmotta/ldp/ldbc/reports")
				.request( JsonLdMessageBodyReaderWriter.JSONLD_MEDIATYPE );

		Method m;
		try {
			m = b.getClass().getDeclaredMethod("request");		
			m.setAccessible(true);

			ClientRequest cr = (ClientRequest) m.invoke(b);

			cr.setEntityType(input.getClass().getGenericSuperclass());

			String authString = db_user + ":" + db_pwd;
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			b.header("Authorization", "Basic " + authStringEnc);
			b.header("Link", "<http://www.w3.org/ns/ldp#Resource>; rel=\"type\"");
			b.header("Slug", "servicereport");
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			//			input = new LinkedList<Node[]>();
			//			((LinkedList<Node[]>) input).add(new Node[] { new BNode(""), RDFS.LABEL, new Literal( "Autocompletion failed: Internal Server Error.", XsdString.DT ) } );
			//			return Response.serverError().entity(new GenericEntity<Iterable<Node[]>>( input ) { }).build();
			return Response.serverError().build();
		}


		/*
		 * send Client Request
		 */

//		JerseyInvocation bla = (JerseyInvocation) b.buildPost(Entity.entity(new GenericEntity<Iterable<Node[]>>( listOfNodes ) { }, JsonLdMessageBodyReaderWriter.JSONLD_MEDIATYPE ));
//		JerseyInvocation bla = (JerseyInvocation) b.buildPost(Entity.entity(new GenericEntity<Iterable<Node[]>>( listOfNodes ) { }, NxMessageBodyReaderWriter.TURTLE_MEDIATYPE ));
		JerseyInvocation bla = (JerseyInvocation) b.buildPost(Entity.entity(new GenericEntity<Iterable<Node[]>>( listOfNodes ) { }, RdfXmlMessageBodyWriter.RDF_XML_MEDIATYPE ));

		try {

			Response response = bla.invoke();
			if (response.getStatus() != 201) throw new ConnectException(response.getStatusInfo().getReasonPhrase());
			Map<String, List<Object>> response_headers = response.getHeaders();

			String location = (String) response_headers.get("Location").get(0);

			List<Node[]> message = new LinkedList<Node[]>();
			message.add(new Node[] { new BNode(""), OWL.SAMEAS, new Resource(location) } );

			return Response.ok(new GenericEntity<Iterable<Node[]>>( message ) { }).build();

		} catch ( Exception  e) {
			e.printStackTrace();
			List<Node[]> error = new ArrayList<Node[]>();
			error.add( new Node[] { new BNode(""), RDFS.LABEL, new Literal("Could not connect to Database.") } );

			return Response.serverError().entity( new GenericEntity<Iterable<Node[]>>( error ) { } ).build();

		}

	}


	@Path("/savereport")
	@POST
	public Response saveReport(@Context UriInfo uriinfo, @Context HttpHeaders headers, Iterable<Node[]> input) {

		//TODO: weiterleitung auf saveReport(String)

		return Response.serverError().entity("Method not yet implemented").build();
	}






	@Path("dictionary")
	@POST
	public Response postDictionary(@Context UriInfo uriinfo, String input, @Context HttpHeaders headers) throws ClientProtocolException, IOException {

		//		return postDictionary_federated(uriinfo, input, headers);
		return postDictionary_notfederated(uriinfo, input, headers);
	}





	@Path("dictionary_notfederated")
	@POST
	public Response postDictionary_notfederated(@Context UriInfo uriinfo, String input, @Context HttpHeaders headers) throws ClientProtocolException, IOException 
	{



		/*
		 * Client Request abschicken
		 */
		List<Node[]> client_request_data = new ArrayList<Node[]>();
		client_request_data.add(new Node[] { new Resource(""), STEP.hasInformation, new Literal(input, XsdString.DT)} );



		List<Node[]> list = new ArrayList<Node[]>();
		list.add(new Node[] { new BNode(""), RDF.TYPE, new Resource(STEP.NS + "Autocompletion" ) });



		String query = input;
		if (query == null || query.isEmpty()) {
			list.add(new Node[] { new BNode(""), RDFS.LABEL, new Literal( "Autocompletion failed: query is empty. "
					+ "Please insert one triple with " + STEP.hasInformation.toString() + " predicate and query as literal.", XsdString.DT ) } );
			return Response.ok().entity(
					new GenericEntity<Iterable<Node[]>>( list )  { } ).build();
		}


		LuceneSearcher searcher = new LuceneSearcher(_ctx.getRealPath("") + indexLocation);
		list.addAll( searcher.search(query) );





		// ausgehende Daten (data) manipulieren
		String output_data = "";
		Map<List<Node[]>, Float> map = new LinkedHashMap<List<Node[]>, Float>();
		List<Node[]> old_nodes = new ArrayList<Node[]>();
		Float old_score = 0f;

		for ( Node[] new_node : list ) {
			if ( !old_nodes.isEmpty() && !new_node[0].toString().equalsIgnoreCase(old_nodes.get(0)[0].toString())) {
				map.put( old_nodes, old_score );
				old_nodes = new ArrayList<Node[]>();
				old_score = 0f;

			}

			if (new_node[1].toString().equalsIgnoreCase(RDF.TYPE.toString())) {
				old_nodes.add(new_node);
			} else if (new_node[1].toString().equalsIgnoreCase(RDFS.LABEL.toString())) {
				old_nodes.add(new_node);
			} else if (new_node[1].toString().equalsIgnoreCase(STEP.hasScore.toString())) {
				old_score = Float.valueOf( ((Literal) new_node[2]).getLabel() );
			} 

		}
		map.put( old_nodes, old_score );

		map = sortByValue(map);

		int counter = 0;
		for ( List<Node[]> nodes :  map.keySet() ) {
			//			if (counter > 4 ) break;

			Float score = map.get(nodes);

			String wordclass = "";
			String classes = "";
			List<String> labels = new ArrayList<String>();
			for ( Node[] node : nodes  ) {

				if (node[1].toString().equalsIgnoreCase(RDFS.LABEL.toString())) {
					//					if ( ((Literal) node[2]).getLanguageTag().equalsIgnoreCase("de") )
					labels.add( ((Literal) node[2]).getLabel() );  
					continue;
				}

				if (node[1].toString().equalsIgnoreCase(RDF.TYPE.toString())) {
					String class_uri = ((Resource) node[2]).getLabel();

					if (class_uri.equalsIgnoreCase("http://www.w3.org/2002/07/owl#NamedIndividual")) {
						wordclass = "substantiv";
					} else if (class_uri.equalsIgnoreCase("http://www.w3.org/2002/07/owl#ObjectProperty")) {
						wordclass = "verb";
					} else if (class_uri.equalsIgnoreCase("http://www.w3.org/2002/07/owl#Class")) {
						wordclass = "entity";
					} else if (class_uri.equalsIgnoreCase("http://www.w3.org/2002/07/owl#FunctionalProperty")) {
						wordclass = "prop";
					} else {
						if (!classes.contains(class_uri)) classes +=  class_uri + "§";  
					}

					//					if ( ((Literal) node[2]).getLanguageTag().equalsIgnoreCase("de") )

				}
			}

			for (String label : labels ) {
				output_data += label;
				if (!wordclass.isEmpty()) {output_data += "@" + wordclass;} else {output_data += "@"; }
				if (!classes.isEmpty()) output_data += "@" + classes.substring(0, classes.length() - 1 ) ;
				output_data += ", "; 
			}
			counter++;
		}

		if (output_data.isEmpty()) {
			return Response.ok().build();
		} else {
			return Response.ok( output_data.substring(0, output_data.length() - 2) ).build();
		}
	}








	@Path("dictionary_federated")
	@POST
	public Response postDictionary_federated(@Context UriInfo uriinfo, String input, @Context HttpHeaders headers) throws ClientProtocolException, IOException 
	{

		/*
		 * Client Request bauen
		 */
		String baseURI = "http://" + uriinfo.getAbsolutePath().getHost().toString() + ":" + uriinfo.getAbsolutePath().getPort();
		Client client = ClientBuilder.newBuilder().register(JsonLdMessageBodyReaderWriter.class).build();

		JerseyInvocation.Builder b = (org.glassfish.jersey.client.JerseyInvocation.Builder) client
				//				.target("http://localhost:8080/SemanticAnnotator/annotator/autocomplete")
				//				.target("http://scc-cnor-129py5.scc.kit.edu:8080/SemanticAnnotator/annotator/autocomplete")
				.target( baseURI + "/SemanticAnnotator/annotator/autocomplete")
				.request( JsonLdMessageBodyReaderWriter.JSONLD_MEDIATYPE );

		Method m;
		try {
			m = b.getClass().getDeclaredMethod("request");		
			m.setAccessible(true);// Abracadabra 

			ClientRequest cr = (ClientRequest) m.invoke(b);// now its OK

			cr.setEntityType(input.getClass().getGenericSuperclass());
			cr.accept( JsonLdMessageBodyReaderWriter.JSONLD_MEDIATYPE );

		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			//			input = new LinkedList<Node[]>();
			//			((LinkedList<Node[]>) input).add(new Node[] { new BNode(""), RDFS.LABEL, new Literal( "Autocompletion failed: Internal Server Error.", XsdString.DT ) } );
			//			return Response.serverError().entity(new GenericEntity<Iterable<Node[]>>( input ) { }).build();
			return Response.serverError().encoding(input).build();
		}


		/*
		 * Client Request abschicken
		 */
		List<Node[]> client_request_data = new ArrayList<Node[]>();
		client_request_data.add(new Node[] { new Resource(""), STEP.hasInformation, new Literal(input, XsdString.DT)} );
		JerseyInvocation bla = (JerseyInvocation) b.buildPost(Entity.entity(new GenericEntity<Iterable<Node[]>>( client_request_data ) { },JsonLdMessageBodyReaderWriter.JSONLD_MEDIATYPE ));
		Response response = bla.invoke();





		/*
		 * Response des Client Requests lesen
		 */
		Iterable<Node[]> data = response.readEntity(  new GenericType<Iterable<Node[]>>(  ) { } ) ;		



		// TODO ausgehende Daten (data) manipulieren
		String output_data = "";
		Map<List<Node[]>, Float> map = new LinkedHashMap<List<Node[]>, Float>();
		List<Node[]> old_nodes = new ArrayList<Node[]>();
		Float old_score = 0f;

		for ( Node[] new_node : data ) {
			if ( !old_nodes.isEmpty() && !new_node[0].toString().equalsIgnoreCase(old_nodes.get(0)[0].toString())) {
				map.put( old_nodes, old_score );
				old_nodes = new LinkedList<Node[]>();
				old_score = 0f;

			}

			if (new_node[1].toString().equalsIgnoreCase(RDF.TYPE.toString())) {
				old_nodes.add(new_node);
			} else if (new_node[1].toString().equalsIgnoreCase(RDFS.LABEL.toString())) {
				old_nodes.add(new_node);
			} else if (new_node[1].toString().equalsIgnoreCase(STEP.hasScore.toString())) {
				old_score = Float.valueOf( ((Literal) new_node[2]).getLabel() );
			} 

		}
		map.put( old_nodes, old_score );

		map = sortByValue(map);

		int counter = 0;
		for ( List<Node[]> nodes :  map.keySet() ) {
			//			if (counter > 4 ) break;

			Float score = map.get(nodes);

			String wordclass = "";
			String classes = "";
			List<String> labels = new ArrayList<String>();
			for ( Node[] node : nodes  ) {

				if (node[1].toString().equalsIgnoreCase(RDFS.LABEL.toString())) {
					//					if ( ((Literal) node[2]).getLanguageTag().equalsIgnoreCase("de") )
					labels.add( ((Literal) node[2]).getLabel() );  
					continue;
				}

				if (node[1].toString().equalsIgnoreCase(RDF.TYPE.toString())) {
					String class_uri = ((Resource) node[2]).getLabel();

					if (class_uri.equalsIgnoreCase("http://www.w3.org/2002/07/owl#NamedIndividual")) {
						wordclass = "substantiv";
					} else if (class_uri.equalsIgnoreCase("http://www.w3.org/2002/07/owl#ObjectProperty")) {
						wordclass = "verb";
					} else if (class_uri.equalsIgnoreCase("http://www.w3.org/2002/07/owl#Class")) {
						wordclass = "entity";
					} else if (class_uri.equalsIgnoreCase("http://www.w3.org/2002/07/owl#FunctionalProperty")) {
						wordclass = "prop";
					} else {
						if (!classes.contains(class_uri)) classes +=  class_uri + "§";  
					}

					//					if ( ((Literal) node[2]).getLanguageTag().equalsIgnoreCase("de") )

				}
			}

			for (String label : labels ) {
				output_data += label;
				if (!wordclass.isEmpty()) {output_data += "@" + wordclass;} else {output_data += "@"; }
				if (!classes.isEmpty()) output_data += "@" + classes.substring(0, classes.length() - 1 ) ;
				output_data += ", "; 
			}
			counter++;
		}

		if (output_data.isEmpty()) {
			return Response.ok().build();
		} else {
			return Response.ok( output_data.substring(0, output_data.length() - 2) ).build();
		}
	}



	/**
	 * 
	 * @param collection
	 * @param uriinfo
	 * @param input
	 * 
	 * @return
	 */
	@POST
	//@RedirectMissingTrailingSlash
	public Response postResource(@PathParam("collection") String collection, @Context UriInfo uriinfo,
			Iterable<Node[]> input) {


		// TODO eingehende Daten (input) manipulieren






		/*
		 * Client Request bauen
		 */

		Client client = ClientBuilder.newBuilder().register(JsonLdMessageBodyReaderWriter.class).build();

		JerseyInvocation.Builder b = (org.glassfish.jersey.client.JerseyInvocation.Builder) client
				//				.target("http://localhost:8080/SemanticAnnotator/autocomplete")
				.target("http://scc-cnor-129py5.scc.kit.edu:8080/SemanticAnnotator/annotator/autocomplete")
				.request( JsonLdMessageBodyReaderWriter.JSONLD_MEDIATYPE );

		Method m;
		try {
			m = b.getClass().getDeclaredMethod("request");		
			m.setAccessible(true);// Abracadabra 

			ClientRequest cr = (ClientRequest) m.invoke(b);// now its OK

			cr.setEntityType(input.getClass().getGenericSuperclass());
			cr.accept( JsonLdMessageBodyReaderWriter.JSONLD_MEDIATYPE );
			System.out.println(cr.getEntityType());

		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			input = new LinkedList<Node[]>();
			((LinkedList<Node[]>) input).add(new Node[] { new BNode(""), RDFS.LABEL, new Literal( "Autocompletion failed: Internal Server Error.", XsdString.DT ) } );
			return Response.serverError().entity(new GenericEntity<Iterable<Node[]>>( input ) { }).build();
		}







		/*
		 * Client Request abschicken
		 */
		JerseyInvocation bla = (JerseyInvocation) b.buildPost(Entity.entity(new GenericEntity<Iterable<Node[]>>( input ) { },JsonLdMessageBodyReaderWriter.JSONLD_MEDIATYPE ));
		Response response = bla.invoke();





		/*
		 * Response des Client Requests lesen
		 */
		Iterable<Node[]> data = response.readEntity(  new GenericType<Iterable<Node[]>>(  ) { } ) ;		
		//		for (Iterator<Node[]> iter = data.iterator(); iter.hasNext() ; ) {
		//			Node[] node = iter.next();
		//			System.out.println("Node: " + node[0] + node[1] + node[2]);
		//		}



		// TODO ausgehende Daten (data) manipulieren



		return Response.ok(new GenericEntity<Iterable<Node[]>>( data ) { }).build();
	}



	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return map.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(
						Map.Entry::getKey, 
						Map.Entry::getValue, 
						(e1, e2) -> e1, 
						LinkedHashMap::new
						));
	}
}
