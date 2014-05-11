/*
 * Author: Pascal & Usman
 */
package com.parking.parkspotter;

public class Parkade {
	
	//declaring all the variables
	private String _parkd_name = null;
	private String _parkd_description = null;
	private String _parkd_rssfeed_url = null;
	private int _parkd_category_id = 0;
	private String _parkd_address = null;
	private int _parkd_nid = 0;
	private String _parkadecount = "";



//Declaring methods and their parameters
	public Parkade()
	{
		 //_parkadecount+=1;
	}
	
	
	
	public int get_parkd_category_id() {
		return _parkd_category_id;
	}
	
	public void set_parkd_category_id(int _parkd_category_id) {
		this._parkd_category_id = _parkd_category_id;
	}
	
	public String get_parkd_address() {
		return _parkd_address;
	}
	
	public void set_parkd_address(String _parkd_address) {
		this._parkd_address = _parkd_address;
	}
	
	public int get_parkd_nid() {
		return _parkd_nid;
	}
	
	public void set_parkd_nid(int _parkd_nid) {
		this._parkd_nid = _parkd_nid;
	}
	
	public String get_parkd_name() {
		return _parkd_name;
	}
	public void set_parkd_name(String parkd_name) {
		this._parkd_name = parkd_name;
	}
	
	public String get_parkd_description() {
		return _parkd_description;
	}
	public void set_parkd_description(String parkd_description) {
		this._parkd_description = parkd_description;
	}
	
	public String get_parkd_rssfeed_url() {
		return _parkd_rssfeed_url;
	}
	public void set_parkd_rssfeed_url(String parkd_rssfeed_url) {
		this._parkd_rssfeed_url = parkd_rssfeed_url;
	}

	public String get_parkadecount() {
		return _parkadecount;
	}

	public void set_parkadecount(String _parkadecount) {
		this._parkadecount = _parkadecount;
	}

	
}