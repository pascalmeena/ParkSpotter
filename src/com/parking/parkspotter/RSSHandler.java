/*
 * Author: Pascal & Usman
 */
package com.parking.parkspotter;


import java.util.ArrayList;
import java.util.List;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.*;

import android.util.Log;

public class RSSHandler extends DefaultHandler 
{
private List<RSSItem> rssItems;
	
	// Used to reference item while parsing
	private RSSItem currentItem;
	
	
	//Parsing content:encoded indicator
	private boolean parsingEncoded;
		
	public RSSHandler() {
		rssItems = new ArrayList<RSSItem>();
	}
	
	public List<RSSItem> getFeed() {
		Log.d("rssItems result: ",rssItems.toString());
		return rssItems;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if ("item".equals(qName)) {
			currentItem = new RSSItem();
		} else if ("content:encoded".equals(qName)) {
			parsingEncoded = true;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ("item".equals(qName)) {
			rssItems.add(currentItem);
			currentItem = null;
		} else if ("content:encoded".equals(qName)) {
			parsingEncoded = false;
		}  
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (parsingEncoded) {
			if (currentItem != null)
				currentItem.set_content_encoded(new String(ch, start, length));
		} 
	}
	
}