package testCases;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import static io.restassured.RestAssured.given;
import org.testng.annotations.Parameters;  
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.testng.annotations.Test;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import static org.hamcrest.Matchers.equalTo;


public class CollectionsTestCases {
	
	String baseUrl = "https://api.getpostman.com/collections";
	String authErrormessage = "Invalid API Key. Every request requires a valid API Key to be sent.";
		
	@Test
	@Parameters({"apiKey"}) 
	public void getAllCollectionsValidKey(String apiKey) {
		//Execute API
		Response response = 
	    	given()
			.header("x-api-key", apiKey)
			.when()
			.get(baseUrl);
		
		//Verify response 
	    	response
			.then()
			.statusCode(200);
		 
		List list = response.jsonPath().getList("collections");
		System.out.println(list.size() + " collections are present");		
				
		for(int i = 0; i < list.size(); i++) {
			System.out.println(" Collection number " + i + " is: " + list.get(i));
			JSONObject jsnobject = new JSONObject((Map) list.get(i));
			assert jsnobject.containsKey("id");
			assert jsnobject.containsKey("name");
			assert jsnobject.containsKey("uid");
			assert jsnobject.containsKey("owner");
		}
	}
	
	
	@Test
	public void getAllCollectionsInvalidKey() {
		//Execute API
		Response response = 
	        given()
			.header("x-api-key", "invalidKey")
			.when()
			.get(baseUrl);
		
		//Verify response 
		response
			.then()
			.statusCode(401)			
			.assertThat()
			.body("error.name", equalTo("AuthenticationError"))
			.body("error.message", equalTo(authErrormessage));	
	}
	
	@Test
	public void getAllCollectionsNoKey() {
		//Execute API
		Response response = 
		given()
			.when()
			.get(baseUrl);
		
		//Verify response 
		response
			.then()
			.statusCode(401)			
			.assertThat()
			.body("error.name", equalTo("AuthenticationError"))
			.body("error.message", equalTo(authErrormessage));		
	}
	
	@Test
	@Parameters({"collectionUid", "apiKey", "collectionName", "postmanId"})
	public void getSingleCollection(String collectionUid, String apiKey, String collectionName, String postmanId) {
		 
		//Execute API
		Response response = 
		given()
			.header("x-api-key", apiKey)
			.pathParam("collectionUid", collectionUid)
			.when()
			.get(baseUrl + "/{collectionUid}");
		
		//Verify response 
		response
			.then()
			.statusCode(200)			
			.assertThat()
			.body("collection.info.name", equalTo(collectionName))
			.body("collection.info._postman_id", equalTo(postmanId));		
	}
	
	@Test(invocationCount = 3)
	@Parameters({"apiKey"})
	public void createCollection(String apiKey) {
		//Prepare required data
		String randomCollectionName = "Name for collection " + (UUID.randomUUID().toString()).substring(5, 15);
		
		Map<String, String> info = new HashMap<>();
		info.put("name", randomCollectionName);
		info.put("schema", "schema name");

		List <Map<String, Object>> items = new ArrayList<>();
		
		Map<String, Object> collection = new HashMap<>();
		collection.put("info", info);
		collection.put("item", items);

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("collection", collection);
		
		System.out.println("Request body is: " + new JSONObject(requestBody));	
		
		//Execute API
		Response response = 
		given()
			.header("x-api-key", apiKey)
			.header("Content-Type", "application/json")
			.when()
			.body(new JSONObject(requestBody))
            		.post(baseUrl);
		
		//Verify response 
		response
			.then()
			.assertThat()
			.statusCode(200);
		
		String bodyStringValue = response.asString();
		Assert.assertTrue(bodyStringValue.contains("id"));
		Assert.assertTrue(bodyStringValue.contains("uid"));
		Assert.assertTrue(bodyStringValue.contains("name"));
		
		JsonPath jsonPathEvaluator = response.jsonPath();
		String name = jsonPathEvaluator.get("collection.name");
		Assert.assertTrue(name.equalsIgnoreCase(randomCollectionName));	
	}
}
