package org.wso2.fasttrack.project.roadlk.ui;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderGeometry;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;

/**
 * Servlet implementation class ActionServlet
 */

public class ActionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ActionServlet() {
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	                                                                              throws ServletException,
	                                                                              IOException {
		// TODO Auto-generated method stub
		String email = request.getParameter("user");
		String location = request.getParameter("location1");

		final Geocoder geocoder = new Geocoder();

		String connectionURL = "jdbc:mysql://localhost/traffic_analyzer_db";

		GeocoderResult results;
		GeocoderGeometry geometry;
		LatLng locationgeo;
		BigDecimal lat = null;
		BigDecimal lng = null;
		double[] geocodes = new double[2];

		GeocoderRequest geocoderRequest =
		                                  new GeocoderRequestBuilder().setAddress(location +
		                                                                                  ", Sri Lanka")
		                                                              .setLanguage("en")
		                                                              .getGeocoderRequest();

		try {
			GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
			results = geocoderResponse.getResults().get(0);
			geometry = results.getGeometry();
			locationgeo = geometry.getLocation();
			lat = locationgeo.getLat();
			lng = locationgeo.getLng();
			Double latitude = lat.doubleValue();
			Double longitude = lng.doubleValue();
			geocodes[0] = latitude;
			geocodes[1] = longitude;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			// out.print(e.getMessage());
		}
		Connection connection = null;
		PreparedStatement pstatement = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			connection = DriverManager.getConnection(connectionURL, "root", "123");
			String queryString =
			                     "INSERT INTO subscribers(email, latitude, longitude) VALUES (\" " +
			                             email + " \", " + geocodes[0] + ", " + geocodes[1] + ")";
			pstatement = connection.prepareStatement(queryString);
			// ResultSet rs = stmt.executeQuery(sql);
			pstatement.execute();
		} catch (Exception ex) {
			// out.println("Unable to connect to batabase.");
		} finally {
			try {
				pstatement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		String Message =
		                 "Your email " + email + " successfully subscribed to <b>" +
		                         location + "</b>";

		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(Message);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	                                                                               throws ServletException,
	                                                                               IOException {
		// TODO Auto-generated method stub

	}
}