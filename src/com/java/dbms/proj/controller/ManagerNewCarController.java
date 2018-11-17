package com.java.dbms.proj.controller;


import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

import com.java.dbms.proj.common.DBFacade;
import com.java.dbms.proj.views.ManagerView;



public class ManagerNewCarController {
	public static void newCar(Scanner input) throws SQLException {
		ManagerView.displayNewCarModel(); //Display page header
		//TODO ask for car model details
		System.out.println("ASK FOR CAR MODEL DETAILS\n");
		
		ResultSet resultSet;
		Statement statement = DBFacade.getConnection().createStatement();

		String make;
		String model;
		String year;
		int amiles = 0,bmiles = 0,cmiles =0;
		int amonths = 0,bmonths = 0,cmonths = 0;
		String servicelista, servicelistb, servicelistc;
		
		int vid = 0, sid = 0, i = 0;
		int mid=0;
		
	
		System.out.println("Please enter Make:");
		make=input.nextLine();
		System.out.println("Please enter Model:");
		model=input.nextLine();
		System.out.println("Please enter Year:");
		year=input.nextLine();
	
		System.out.println("Please enter number of miles for Service A:");
		amiles=input.nextInt();
		System.out.println("Please enter number of months for Service A:");
		amonths=input.nextInt();
		System.out.println("Please enter list of basic services for Service A seperated with a comma:");
		servicelista=input.nextLine();
		servicelista=input.nextLine();
		
		String[] valuesa = servicelista.split(",");
		
		
		System.out.println("Please enter number of miles for Service B:");
		bmiles=input.nextInt();
		System.out.println("Please enter number of months for Service B:");
		bmonths=input.nextInt();
		System.out.println("Please enter list of basic services for Service B seperated with a comma:");
		servicelistb=input.nextLine();
		servicelistb=input.nextLine();	
		
		String[] valuesb = servicelistb.split(",");
		
		System.out.println("Please enter number of miles for Service C:");
		cmiles=input.nextInt();
		System.out.println("Please enter number of months for Service C:");
		cmonths=input.nextInt();
		System.out.println("Please enter list of basic services for Service C seperated with a comma:");
		servicelistc=input.nextLine();	
		servicelistc=input.nextLine();	
		
		String[] valuesc = servicelistb.split(",");
		
				try {
					resultSet = statement.executeQuery( "SELECT VID FROM VEHICLE_TYPE WHERE MAKE = '" + make + "' and MODEL = '" + model + "'" );
					if (resultSet.next())
					{
						vid=resultSet.getInt("VID");
					}					
					
				}
				catch ( SQLException e ) {
					System.out.println( "Invalid Vehicle Type : " + e.getMessage() );
				}
				
		System.out.println( "Please select from the following user options:" );
		System.out.println( "\tEnter '1' to Add Car" );		
		System.out.println( "\tEnter '2' to Go Back" );
		
		String userInput = "";
		do {
			System.out.print( "\nOption Selection : " );
			userInput = input.nextLine();
		}while( !userInput.equals( "1" ) && !userInput.equals( "2" ) );
		
		if( userInput.equals( "1" ) ) {
			//TODO call addCar controller			
			 
			try {
				
				resultSet = statement.executeQuery( "SELECT maintenance_seq.nextval from dual" );
				if ( resultSet.next() ) {
					mid = resultSet.getInt( "NEXTVAL" );
				}
				try {
				statement.executeUpdate( "INSERT INTO MAINTENANCE VALUES ('" + vid + "', '" + amiles + "', '" + amonths + "', '" + mid + "', 'A')" );
				}
				catch ( SQLException e ) {
					System.out.println( "Unable to insert into the MAINTENANCE Table for Service A : " + e.getMessage() );
				
			    }
				
				while(i<valuesa.length) {
					resultSet = statement.executeQuery( "SELECT SERVICE_ID FROM SERVICE_DETAILS WHERE SERVICE_NAME = '"+valuesa[i]+"'");
				
				
				if (resultSet.next())
				{
					sid=resultSet.getInt("SERVICE_ID");
				}
				try {
				statement.executeUpdate( "INSERT INTO MAINTENANCE_SERVICE_MAPPING VALUES ('" + mid + "', '" + sid + "')" );
				}
				catch ( SQLException e ) {
					System.out.println( "Unable to insert into the MAINTENANCE_SERVICE_MAPPING Table for Service A : " + e.getMessage() );
				
			    }
				i++;
				}
			}
			catch ( SQLException e ) {
				System.out.println( "Unable to access the MAINTENANCE Table for Service A : " + e.getMessage() );
			
		    }
			try {
				
			resultSet = statement.executeQuery( "SELECT maintenance_seq.nextval from dual" );
			if ( resultSet.next() ) {
				mid = resultSet.getInt( "NEXTVAL" );
			}
			
			statement.executeUpdate( "INSERT INTO MAINTENANCE VALUES ('" + vid + "', '" + bmiles + "', '" + bmonths + "','" + mid + "', '" + "B" + "')" );
			i=0;
			while(i<valuesb.length) {
				resultSet = statement.executeQuery( "SELECT SERVICE_ID FROM SERVICE_DETAILS WHERE SERVICE_NAME = '"+valuesb[i]+"'");
			
			
			if (resultSet.next())
			{
				sid=resultSet.getInt("SERVICE_ID");
			}
			
			statement.executeUpdate( "INSERT INTO MAINTENANCE_SERVICE_MAPPING VALUES ('" + mid + "', '" + sid + "')" );
			i++;
			}
			}
			catch ( SQLException e ) {
				System.out.println( "Unable to access the MAINTENANCE Table for Service B : " + e.getMessage() );
			
		}
			i=0;
			try {
				
				
			resultSet = statement.executeQuery( "SELECT maintenance_seq.nextval from dual" );
			if ( resultSet.next() ) {
				mid = resultSet.getInt( "NEXTVAL" );
			}
			
			statement.executeUpdate( "INSERT INTO MAINTENANCE VALUES ('" + vid + "', '" + cmiles + "', '" + cmonths + "','" + mid + "', '" + "C" + "')" );
			while(i<valuesc.length) {
				resultSet = statement.executeQuery( "SELECT SERVICE_ID FROM SERVICE_DETAILS WHERE SERVICE_NAME = '"+valuesc[i]+"'");
			
			
			if (resultSet.next())
			{
				sid=resultSet.getInt("SERVICE_ID");
			}
			
			statement.executeUpdate( "INSERT INTO MAINTENANCE_SERVICE_MAPPING VALUES ('" + mid + "', '" + sid + "')" );
			i++;
			}
			
			System.out.println( "ADD CONTROLLER TO ADD NEW CAR" );
		}
			catch ( SQLException e ) {
				System.out.println( "Unable to access the MAINTENANCE Table for Service C : " + e.getMessage() );
			
		}
	}
	}
}
