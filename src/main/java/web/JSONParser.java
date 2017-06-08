package main.java.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

@Path("/jsonld-parser")
public class JSONParser {
	
	/**
	 * http://localhost:8080/semantic-annotator/test/jsonld-parser
	 * @return
	 */
	
	@GET
	public String sayHello() {
		return "Hello!";
	}

	@POST
	@Produces("application/ld+json")
//	@Consumes(MediaType.TEXT_PLAIN)
	public String parseJSONLD(String text) throws IOException, JsonLdError {
		
		Object jsonObject = JsonUtils.fromString(text);
		//Object jsonObject = JsonLdProcessor.fromRDF(text);
		// Create a context JSON map containing prefixes and definitions
		Map<Object, Object> context = new HashMap<Object, Object>();
		context.put("@vocab", "http://schema.org");
		// Customise context...
		// Create an instance of JsonLdOptions with the standard JSON-LD options
		JsonLdOptions options = new JsonLdOptions();
		// Customise options...
		// Call whichever JSONLD function you want! (e.g. compact)
		Map<String, Object> compact = JsonLdProcessor.compact(jsonObject, context, options);
		
		// Print out the result (or don't, it's your call!)
		return JsonUtils.toPrettyString(compact);
	}
	
}
