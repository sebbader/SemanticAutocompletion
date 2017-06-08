package main.java.web;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.util.ConcatenatingIterable;
import org.apache.http.client.ClientProtocolException;
import org.json.*;

@Path("/toJsonLD")
public class Listener {
	@Context
	ServletContext _ctx;

	
	@GET
	public Response getOverview(@Context UriInfo uriinfo, @Context Request request) {

		return Response.ok().build();
	}

	
	@POST
	public Response postWords(@Context UriInfo uriinfo, String input, @Context HttpHeaders headers) throws ClientProtocolException, IOException {
		JSONObject jsonObj = new JSONObject(input);
		List<Node[]> listOfNodes = new LinkedList<Node[]>();
		listOfNodes.add(new Node[] // Name
		{ new Resource("http://people.aifb.kit.edu/mu2771/step#report1"),
				new Resource("http://people.aifb.kit.edu/mu2771/step#wasCreatedBy"),
				new Literal(jsonObj.getString("Name")) });
		listOfNodes.add(new Node[] // Duration
		{ new Resource("http://people.aifb.kit.edu/mu2771/step#report1"),
				new Resource("http://people.aifb.kit.edu/mu2771/step#lastedFor"),
				new Literal(jsonObj.getString("Duration")) });
		listOfNodes.add(new Node[] // Cost
		{ new Resource("http://people.aifb.kit.edu/mu2771/step#report1"),
				new Resource("http://people.aifb.kit.edu/mu2771/step#costs"),
				new Literal(jsonObj.getString("Cost")) });
		listOfNodes.add(new Node[] // Complexity
		{ new Resource("http://people.aifb.kit.edu/mu2771/step#report1"),
				new Resource("http://people.aifb.kit.edu/mu2771/step#hadComplexity"),
				new Literal(jsonObj.getString("Complexity")) });
		listOfNodes.add(new Node[] // Info
		{ new Resource("http://people.aifb.kit.edu/mu2771/step#report1"),
				new Resource("http://people.aifb.kit.edu/mu2771/step#hasInformation"),
				new Literal(jsonObj.getString("Info")) });
		Iterable<Node[]> nx = new ConcatenatingIterable<Node[]>(listOfNodes);
		
		//SendPostRequest("http://localhost:8080/rwldresources/GetEntity", new GenericEntity<Iterable<Node[]>>(nx) {});
		return Response.ok(new GenericEntity<Iterable<Node[]>>(nx) {}).build();
	}


}
