/*
 * Author: Pascal & Usman
 */
package com.parking.parkspotter;

public class RSSItem 
{
	private String _content_encoded;
//creating methods and their parameters	

	public String get_content_encoded() {
		return _content_encoded;
	}

	public void set_content_encoded(String _content_encoded) {
		this._content_encoded = _content_encoded;
	}
	
	@Override
	public String toString() {
		
		return _content_encoded;
	}
}
