package com.java.dbms.proj.controller;

import java.sql.ResultSet;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import com.java.dbms.proj.common.DBFacade;
import com.java.dbms.proj.entities.Maintenance;
import com.java.dbms.proj.entities.Part;
import com.java.dbms.proj.entities.Repair;
import com.java.dbms.proj.entities.Service;

public class ReceptionistInvoiceController {
	public static void invoice(Scanner input) {

		com.java.dbms.proj.views.ReceptionistView.displayInvoice(); // Display page header

		/* Necessary Variables for SQL Transactions */
		Statement statement = null;
		ResultSet resultSet;

		int customerID = -1;
		String userInput = "";
		String serviceType = "";

		ArrayList<Service> appointments = new ArrayList<Service>();

		try {
			/* get a conection to the database */
			statement = DBFacade.getConnection().createStatement();

			System.out.print("\nEnter the email of the customer invoice that you would like to search : ");
			userInput = input.nextLine();
		} catch (SQLException e) {
			System.out.println("Error in acquiring the database connection : " + e.getMessage());
			return;
		}

		/* Find Customer */
		try {
			resultSet = statement.executeQuery("SELECT * FROM CUSTOMER WHERE EMAIL = '" + userInput + "' AND SC_ID = '"
					+ ApplicationController.employee.getServiceCenterId() + "'");
			if (resultSet.next()) {
				/* Find Customer Appointment */
				customerID = resultSet.getInt("CID");
			} else {
				System.out.println("There is no Customer associated with the given email : " + userInput + "\n");
				return;
			}
		} catch (SQLException e) {
			System.out.println("Unable to access the Customer Table : " + e.getMessage() + ": Transaction Aborted");
			return;
		}

			/* Find all Appointments that coorespond with this customer */
			try {
				resultSet = statement.executeQuery(
						"SELECT * FROM APPOINTMENT, CUSTOMER WHERE CUSTOMER.CID = Appointment.CUSTOMER_ID AND CUSTOMER.CID = '"
								+ customerID + "' AND STATE = 'COMPLETE' ORDER BY SERVICE_TYPE");
				while (resultSet.next()) { // while there is an appointment tuple create the services
					Service service = null;
					serviceType = resultSet.getString("SERVICE_TYPE");

					/* Assign Service Type */
					if (serviceType.equalsIgnoreCase("repair")) {
						service = new Repair();
					} else {
						service = new Maintenance();
					}

					/* Assign Service Attributes from Appointment Table */
					service.setServiceType(serviceType);
					service.setAppointmentID(resultSet.getString("APPOINTMENT_ID"));
					service.setCustomerID(resultSet.getInt("CID"));
					service.setVehicleLicense(resultSet.getString("VEHICLE_LICENSE"));
					service.setAppointmentDate(resultSet.getString("APPOINTMENT_DATE"));
					service.setRequestedMechanic(resultSet.getString("REQUESTED_MECHANIC"));
					service.setMechanicID(resultSet.getInt("MECHANIC_ID"));
					service.setServiceStatus(resultSet.getString("STATE"));
					service.setServiceTypeID(resultSet.getString("SERVICE_TYPE_ID"));
					service.setServiceCenterID(resultSet.getString("SC_ID"));
					appointments.add(service);
				}
			} catch (SQLException e) {
				System.out
						.println("Unable to access the Appointment Table : " + e.getMessage() + " : Transaction Aborted\n");
				return;
			}

			if (appointments.size() == 0) {
				System.out.println("There are no 'Completed' appointments associationed with this customer.\n");
				return;
			}

			for (int index = 0; index < appointments.size(); index++) {
				/* Get TimeSlot Details & Mechanic ID */
				try {
					resultSet = statement.executeQuery("SELECT * FROM TIME_SLOT WHERE APPOINTMENT_ID = '"
							+ appointments.get(index).getAppointmentID() + "' ");
					if (resultSet.next()) {
						appointments.get(index).createTimeSlot(resultSet.getInt("SLOT_ID"),
								resultSet.getString("START_TIME"), resultSet.getString("END_TIME"));
						appointments.get(index).setMechanicID(resultSet.getInt("MECHANIC_ID"));
					} else {
						System.out.println("There are no Times associated with this appointment : "
								+ appointments.get(index).getAppointmentID() + "\n");
						break;
					}
				} catch (SQLException e) {
					System.out.println("Unable to access Time Slot Table : " + e.getMessage() + " : Transaction Aborted\n");
					return;
				}

				try {
					/* Get Hourly Rate & Mechanic Name */
					resultSet = statement.executeQuery(
							"SELECT * FROM HOURLY_EMPLOYEE,EMPLOYEE WHERE HOURLY_EMPLOYEE.EID = EMPLOYEE.EID AND EMPLOYEE.EID = '"
									+ appointments.get(index).getMechanicID() + "'");
					if (resultSet.next()) {
						appointments.get(index).setMechanicCost(resultSet.getDouble("HOURLY_RATE"));
						appointments.get(index).setMechanicFullName(
								resultSet.getString("FIRSTNAME") + " " + resultSet.getString("LASTNAME"));
					} else {
						System.out.println("There is no mechanic associated with this appointment : "
								+ appointments.get(index).getAppointmentID() + "\n");
						break;
					}
				} catch (SQLException e) {
					System.out.println(
							"Unable to access Hourly Employee Table : " + e.getMessage() + " : Transaction Aborted\n");
					return;
				}

				/* Find Vehicle ID */
				try {
					resultSet = statement.executeQuery(
							"SELECT * FROM VEHICLE,VEHICLE_TYPE WHERE VEHICLE.VID = VEHICLE_TYPE.VID AND VEHICLE.LICENSE = '" + appointments.get(index).getVehicleLicense() + "'");
					if (resultSet.next()) {
						appointments.get(index).setVid(resultSet.getInt("VID"));
						appointments.get(index).make = resultSet.getString("MAKE");
						appointments.get(index).setFirstAID(resultSet.getString("FIRST_A_APPID"));
						appointments.get(index).setFirstBID(resultSet.getString("FIRST_B_APPID"));
						appointments.get(index).setFirstCID(resultSet.getString("FIRST_C_APPID"));
					} else {
						System.out.println("There is no vehicle associated with the given car scheduled for appointment : "
								+ appointments.get(index).getAppointmentID());
						break;
					}
				} catch (SQLException e) {
					System.out
							.println("Unable to access the Vehicle Table : " + e.getMessage() + " : Transaction Aborted\n");
					return;
				}

				if (appointments.get(index).getServiceType().equalsIgnoreCase("repair")) {
					try {
						/* get repair details */
						resultSet = statement.executeQuery(
								"SELECT * FROM REPAIR WHERE RID = '" + appointments.get(index).getServiceTypeID() + "' ");
						if (resultSet.next()) {
							((Repair) appointments.get(index)).setDiagnosis(resultSet.getString("DIAGNOSTIC"));
							((Repair) appointments.get(index)).setDiagnosisFee(resultSet.getDouble("DIAGNOSTIC_FEE"));
							((Repair) appointments.get(index)).setRepairID(resultSet.getString("RID"));
							((Repair) appointments.get(index)).setRepairName(resultSet.getString("REPAIR_NAME"));
						} else {
							System.out.println("There is no repair service associated with this appointment : "
									+ appointments.get(index).getAppointmentID() + "\n");
							break;
						}
					} catch (SQLException e) {
						System.out.println(
								"Uable to access the Repair Table : " + e.getMessage() + " : Transaction Aborted\n");
						return;
					}

					ArrayList<String> repairs = new ArrayList<String>();
					/* Get services required */
					try {
						resultSet = statement.executeQuery("SELECT REQUIRED_SERVICE_ID FROM REPAIR_SERVICE_MAPPING"
								+ " WHERE RID = '" + ((Repair) appointments.get(index)).getServiceTypeID() + "' ");
						while (resultSet.next()) {
							repairs.add(resultSet.getString("REQUIRED_SERVICE_ID"));
						}
					} catch (SQLException e) {
						System.out.println("Unable to access the Required Service Table : " + e.getMessage()
								+ " : Transaction Aborted\n");
						return;
					}

					for (int i = 0; i < repairs.size(); i++) {
						try {
							resultSet = statement.executeQuery("SELECT SERVICE_NAME, PART_ID, TIME_REQUIRED, CHARGE_TYPE"
									+ " FROM SERVICE_DETAILS" + " WHERE SERVICE_ID = '" + repairs.get(i) + "'");
							while (resultSet.next()) {
								Part part = new Part();
								part.setRequiredFor(resultSet.getString("SERVICE_NAME"));
								part.setPartID(resultSet.getInt("PART_ID"));
								part.setInstallTime(resultSet.getDouble("TIME_REQUIRED"));
								part.setChargeType(resultSet.getString("CHARGE_TYPE"));
								part.setAssociatedService(repairs.get(i));

								((Repair) appointments.get(index)).getPartsList().add(part);
							}
						} catch (SQLException e) {
							System.out.println("Unable to access the Service Details Table : " + e.getMessage()
									+ " : Transaction Aborted\n");
							return;
						}
					}

					for (int i = 0; i < ((Repair) appointments.get(index)).getPartsList().size(); i++) {
						try {
							/* Get number of this parts required */
							resultSet = statement.executeQuery("SELECT QUANTITY FROM PARTS_QUANTITY WHERE VID = '"
									+ ((Repair) appointments.get(index)).getVid() + "' AND PART_ID = '"
									+ ((Repair) appointments.get(index)).getPartsList().get(i).getPartID() + "' AND SERVICE_ID = '" +
									((Repair) appointments.get(index)).getPartsList().get(i).getAssociatedService() + "'");
							if (resultSet.next()) {
								((Repair) appointments.get(index)).getPartsList().get(i)
										.setUnitsRequired(resultSet.getInt("QUANTITY"));
							} else {
								System.out.println("Unable to acquire the number of parts needed for this appointment : "
										+ appointments.get(index).getAppointmentID() + "\n");
								break;
							}
						} catch (SQLException e) {
							System.out.println(
									"Unable to access Quantity Table : " + e.getMessage() + " : Aborting Transaction\n");
							return;
						}
					}

					for (int i = 0; i < ((Repair) appointments.get(index)).getPartsList().size(); i++) {
						try {
							/* get low/high cost */
							resultSet = statement
									.executeQuery("SELECT CHARGE_VALUE FROM SERVICE_CHARGE WHERE CHARGE_TYPE = '"
											+ ((Repair) appointments.get(index)).getPartsList().get(i).getChargeType()
											+ "' ");
							if (resultSet.next()) {
								((Repair) appointments.get(index)).getPartsList().get(i)
										.setInstallCharge(resultSet.getDouble("CHARGE_VALUE"));
							} else {
								System.out.println("There is no value associated with this charge type.\n");
								break;
							}
						} catch (SQLException e) {
							System.out.println(
									"Unable to access Charge Table : " + e.getMessage() + " : Aborting Transaction\n");
							return;
						}
					}

					for (int i = 0; i < ((Repair) appointments.get(index)).getPartsList().size(); i++) {
						try {
							resultSet = statement.executeQuery("SELECT * FROM ACME_INVENTORY WHERE SC_ID = '"
									+ ApplicationController.employee.getServiceCenterId() + "' AND PART_ID = '"
									+ ((Repair) appointments.get(index)).getPartsList().get(i).getPartID() + "' AND MAKE = '" + appointments.get(index).make + "'");
							if (resultSet.next()) {
								((Repair) appointments.get(index)).getPartsList().get(i)
										.setPartName(resultSet.getString("PART_NAME"));
								((Repair) appointments.get(index)).getPartsList().get(i)
										.setUnitCost(resultSet.getDouble("UNIT_COST"));
							} else {
								System.out.println("There is no unit cost associated with this part.\n");
							}
						} catch (SQLException e) {
							System.out.println("Unable to access the Inventory Table : " + e.getMessage()
									+ " : Aborting Transaction\n");
							return;
						}
					}

					/* Calculate Part Cost and Time */
					for (int j = 0; j < ((Repair) appointments.get(index)).getPartsList().size(); j++) {
						double unitCost = ((Repair) appointments.get(index)).getPartsList().get(j).getUnitCost();
						double unitsRequired = ((Repair) appointments.get(index)).getPartsList().get(j).getUnitsRequired();
						appointments.get(index).addToPartsCost(unitCost * unitsRequired);
						appointments.get(index).addToInstallationCost(
								((Repair) appointments.get(index)).getPartsList().get(j).getInstallCharge());
						appointments.get(index)
								.addToTotalHours(((Repair) appointments.get(index)).getPartsList().get(j).getInstallTime());
					}
					
				} else {
					try {
						/* get maintenance details */
						resultSet = statement.executeQuery("SELECT * FROM MAINTENANCE WHERE VID = '"
								+ ((Maintenance) appointments.get(index)).getVid() + "' AND MAINTENANCE_NAME = '"
								+ ((Maintenance) appointments.get(index)).getServiceTypeID() + "' ");
						if (resultSet.next()) {
							((Maintenance) appointments.get(index)).setMiles(resultSet.getInt("MILES"));
							((Maintenance) appointments.get(index)).setMonths(resultSet.getInt("MONTHS"));
							((Maintenance) appointments.get(index)).setMaintenanceID(resultSet.getString("MAINTENANCE_ID"));
							((Maintenance) appointments.get(index))
									.setMaintenanceName(resultSet.getString("MAINTENANCE_NAME"));
						} else {
							System.out.println("There is no maintenance service associated with this appointment : "
									+ appointments.get(index).getAppointmentID() + "\n");
							break;
						}
					} catch (SQLException e) {
						System.out.println(
								"Uable to access the Maintenance Table : " + e.getMessage() + " : Aborting Transaction\n");
						return;
					}

					ArrayList<String> repairs = new ArrayList<String>();
					/* Get services required */
					try {
						resultSet = statement.executeQuery("SELECT SERVICE_ID FROM MAINTENANCE_SERVICE_MAPPING"
								+ " WHERE M_ID = '" + ((Maintenance) appointments.get(index)).getMaintenanceID() + "' ");
						while (resultSet.next()) {
							repairs.add(resultSet.getString("SERVICE_ID"));
						}
					} catch (SQLException e) {
						System.out.println("Unable to access the Required Maintenance Table : " + e.getMessage()
								+ " : Transaction Aborted\n");
						return;
					}
					for (int i = 0; i < repairs.size(); i++) {
						try {
							resultSet = statement.executeQuery("SELECT SERVICE_NAME, PART_ID, TIME_REQUIRED, CHARGE_TYPE"
									+ " FROM SERVICE_DETAILS" + " WHERE SERVICE_ID = '" + repairs.get(i) + "'");
							while (resultSet.next()) {
								Part part = new Part();
								part.setRequiredFor(resultSet.getString("SERVICE_NAME"));
								part.setPartID(resultSet.getInt("PART_ID"));
								part.setInstallTime(resultSet.getDouble("TIME_REQUIRED"));
								part.setChargeType(resultSet.getString("CHARGE_TYPE"));
								part.setAssociatedService(repairs.get(i));

								((Maintenance) appointments.get(index)).getPartsList().add(part);
							}
						} catch (SQLException e) {
							System.out.println("Unable to access the Service Details Table : " + e.getMessage()
									+ " : Transaction Aborted\n");
							return;
						}
					}

					for (int i = 0; i < ((Maintenance) appointments.get(index)).getPartsList().size(); i++) {
						try {
							/* Get number of this part required */
							resultSet = statement.executeQuery("SELECT QUANTITY FROM PARTS_QUANTITY WHERE VID = '"
									+ ((Maintenance) appointments.get(index)).getVid() + "' AND PART_ID = '"
									+ ((Maintenance) appointments.get(index)).getPartsList().get(i).getPartID() + "' AND SERVICE_ID = '" +
									((Maintenance) appointments.get(index)).getPartsList().get(i).getAssociatedService() + "'");
							if (resultSet.next()) {
								((Maintenance) appointments.get(index)).getPartsList().get(i)
										.setUnitsRequired(resultSet.getInt("QUANTITY"));
							} else {
								System.out.println("Unable to acquire the number of parts needed for this : "
										+ appointments.get(index).getAppointmentID() + "\n");
								break;
							}
						} catch (SQLException e) {
							System.out.println(
									"Unable to access Quantity Table : " + e.getMessage() + " : Aborting Transaction\n");
							return;
						}
					}

					for (int i = 0; i < ((Maintenance) appointments.get(index)).getPartsList().size(); i++) {
						try {
							/* get low/high cost */
							resultSet = statement
									.executeQuery("SELECT CHARGE_VALUE FROM SERVICE_CHARGE WHERE CHARGE_TYPE = '"
											+ ((Maintenance) appointments.get(index)).getPartsList().get(i).getChargeType()
											+ "' ");
							if (resultSet.next()) {
								((Maintenance) appointments.get(index)).getPartsList().get(i)
										.setInstallCharge(resultSet.getDouble("CHARGE_VALUE"));
							} else {
								System.out.println("There is no value associated with this charge type.\n");
								break;
							}
						} catch (SQLException e) {
							System.out.println(
									"Unable to access Charge Table : " + e.getMessage() + " : Aborting Transaction\n");
							return;
						}
					}

					for (int i = 0; i < ((Maintenance) appointments.get(index)).getPartsList().size(); i++) {
						try {
							resultSet = statement.executeQuery("SELECT * FROM ACME_INVENTORY WHERE SC_ID = '"
									+ ApplicationController.employee.getServiceCenterId() + "' AND PART_ID = '"
									+ ((Maintenance) appointments.get(index)).getPartsList().get(i).getPartID() + "' ");
							if (resultSet.next()) {
								((Maintenance) appointments.get(index)).getPartsList().get(i)
										.setPartName(resultSet.getString("PART_NAME"));
								((Maintenance) appointments.get(index)).getPartsList().get(i)
										.setUnitCost(resultSet.getDouble("UNIT_COST"));
							} else {
								System.out.println("There is not unit cost associated with this part.");
								break;
							}
						} catch (SQLException e) {
							System.out.println("Unable to access the Inventory Table : " + e.getMessage()
									+ " : Aborting Transaction\n");
							return;
						}
					}

					/* Calculate Part Cost and Time */
					for (int j = 0; j < ((Maintenance) appointments.get(index)).getPartsList().size(); j++) {
						double unitCost = ((Maintenance) appointments.get(index)).getPartsList().get(j).getUnitCost();
						double unitsRequired = ((Maintenance) appointments.get(index)).getPartsList().get(j)
								.getUnitsRequired();

						appointments.get(index).addToPartsCost(unitCost * unitsRequired);
						appointments.get(index).addToInstallationCost(
								((Maintenance) appointments.get(index)).getPartsList().get(j).getInstallCharge());
						appointments.get(index).addToTotalHours(
								((Maintenance) appointments.get(index)).getPartsList().get(j).getInstallTime());
					}
				}
			System.out.println(
					"\n---------------------------------------------------------------------------------------");
			System.out.println("Service Center : " + appointments.get(index).getServiceCenterID() + " Invoice Details");
			System.out
					.println("---------------------------------------------------------------------------------------");
			System.out.println(appointments.get(index).toCustomerString());
			if (appointments.get(index).getServiceType().equalsIgnoreCase("repair"))
				System.out.println(((Repair) appointments.get(index)).repairPartsToString());
			else
				System.out.println(((Maintenance) appointments.get(index)).repairPartsToString());
			System.out.println(
					"\n***************************************************************************************");
		}


		System.out.println("\nPlease select from the following user options:");
		System.out.println("\tEnter '1' to Go Back");

		userInput = "";
		do {
			System.out.print("\nOption Selection : ");
			userInput = input.nextLine();
		} while (!userInput.equals("1"));
	}
}