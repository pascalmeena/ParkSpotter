/*
 * Author: Pascal & Usman
 */
package com.parking.parkspotter;

import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class RSSFeed 
{
	private String rssUrl;

	/**
	 * Constructor
	 * 
	 * @param rssUrl
	 */
	public RSSFeed(String rssUrl) {
		this.rssUrl = rssUrl;
	}

	/**
	 * Get RSS items.
	 * 
	 * @return
	 */
	public List<RSSItem> getItems() throws Exception {
		
		// SAX parse RSS data
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		RSSHandler handler = new RSSHandler();
		
		saxParser.parse(rssUrl, handler);

		return handler.getFeed();
		
	}
}
