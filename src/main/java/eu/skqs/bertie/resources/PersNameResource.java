package eu.skqs.bertie.resources;

import org.apache.uima.resource.SharedResourceObject;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.DataResource;


public final class PersNameResource implements SharedResourceObject {
	private String uri;

	public void load(DataResource data) throws ResourceInitializationException {
		uri = data.getUri().toString();
	}

	public String getUri() { return uri; }
}
