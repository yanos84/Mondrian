package mond;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import org.olap4j.metadata.Schema;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;
import org.olap4j.OlapWrapper;
import org.olap4j.Position;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class orr {

	static Connection connection;
	static Schema schema;
	static OlapConnection olapConnection;

	/*
	 * public static void cubeLoad(Schema schema){
	 * //Méthode pour charger un cube de données
	 * try{ Cube cube =schema.getCubes().get("tp2");
	 * System.out.println("Cube ["+cube+"] loaded");
	 * }catch(Exception e){
	 * System.out.println("Something wrong while loading the cube");}
	 * }
	 */

	public static void doFirst() {
		// Pour charger le Driver de mondrian
		try {
			Class.forName("mondrian.olap4j.MondrianOlap4jDriver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Driver not found");
		}
	}

	public static void doConnection(String param) {
		// Pour établir une connexion tomcat vers mondrian
		try {
			connection = DriverManager.getConnection(param);
			OlapWrapper wrapper = (OlapWrapper) connection;
			olapConnection = wrapper.unwrap(OlapConnection.class);
			System.out.println("connection to mondrian : established");
			schema = olapConnection.getOlapSchema();
		} catch (Exception e) {
			System.out.println("something wrong with mondrian!!");
			e.printStackTrace();
		}
		;

	}

	public static void displayQuery(String mdx, String csvFilePath) {
		try (FileWriter writer = new FileWriter(csvFilePath)) {
			OlapStatement statement = olapConnection.createStatement();
			CellSet cellSet = statement.executeOlapQuery(mdx);

			// Extract axes
			List<CellSetAxis> axes = cellSet.getAxes();
			if (axes.size() < 2) {
				throw new RuntimeException("The query should have at least two axes.");
			}

			// Write column headers (Axis 0 members)
			List<Position> columns = axes.get(Axis.COLUMNS.axisOrdinal()).getPositions();
			writer.write("Row Members,"); // Placeholder for row headers
			for (Position column : columns) {
				writer.write(formatPositionMembers(column) + ",");
			}
			writer.write("\n");

			// Write rows and data
			List<Position> rows = axes.get(Axis.ROWS.axisOrdinal()).getPositions();
			for (Position row : rows) {
				writer.write(formatPositionMembers(row) + ","); // Row header
				for (Position column : columns) {
					String cellValue = cellSet.getCell(Arrays.asList(column.getOrdinal(), row.getOrdinal()))
							.getFormattedValue();
					writer.write(cellValue + ",");
				}
				writer.write("\n");
			}

			System.out.println("Query result saved to CSV: " + csvFilePath);
		} catch (IOException e) {
			System.out.println("Error writing to CSV file");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Error while executing MDX query");
			e.printStackTrace();
		}
	}

	private static String formatPositionMembers(Position position) {
		StringBuilder builder = new StringBuilder();
		position.getMembers().forEach(member -> builder.append(member.getName()).append(" "));
		return builder.toString().trim();
	}

	public static void main(String[] args) {
		doFirst();
		doConnection(
				"jdbc:mondrian:Jdbc=jdbc:postgresql://localhost:5432/warehouse?user=postgres&password=microlife; Catalog=file:tp2.xml; JdbcDrivers=org.postgresql.Driver");
		String mdx = "select NON EMPTY [Product].FirstSibling  ON COLUMNS, NON EMPTY [Time].[Month].Members  ON ROWS  from [Sales_Cube]";
		String csvFilePath = "output.csv";
		displayQuery(mdx, csvFilePath);
	}

}