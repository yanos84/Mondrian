package mond;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import org.olap4j.metadata.Schema;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;
import org.olap4j.OlapWrapper;
import org.olap4j.Position;

public class orr {

	static Connection connection;
	static Schema schema;
	static OlapConnection olapConnection; 


	/*public static void cubeLoad(Schema schema){
		//Méthode pour charger un cube de données
		try{ Cube cube =schema.getCubes().get("tp2");
		System.out.println("Cube ["+cube+"] loaded");
		}catch(Exception e){ System.out.println("Something wrong while loading the cube");}
	 }*/



	public static void doFirst(){
		//Pour charger le Driver de mondrian
		try{Class.forName("mondrian.olap4j.MondrianOlap4jDriver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Driver not found");
		}
	}

	public static void doConnection(String param) {
		// Pour établir une connexion tomcat vers mondrian
		try{connection  =DriverManager.getConnection(param);
		OlapWrapper wrapper = (OlapWrapper) connection;
		olapConnection = wrapper.unwrap(OlapConnection.class);
		System.out.println("connection to mondrian : established");
		schema = olapConnection.getOlapSchema();
		}catch (Exception e) {System.out.println("something wrong with mondrian!!");
		e.printStackTrace();};

	}

	public static void displayQuery(String mdx)
	{
		/* TODO Attention: Pour plus de deux dimensions, il faut utiliser List<CellSetAxis> cellSetAxes = cellSet.getAxes();
			 afin de parcourir sur toutes les dimensions les cellules du cube*/


		//permet d'afficher le résultat d'une requéte MDX 
		/*Le résultat est seulement pour deux dimensions columns et rows. 
		 */
		try{
			OlapStatement statement = olapConnection.createStatement();
			CellSet cellSet = statement.executeOlapQuery(mdx);
			//System.out.println(cellSet.getCell(88).getFormattedValue());

			for (Position axis_0:cellSet.getAxes().get( Axis.COLUMNS.axisOrdinal() ).getPositions()) 
			{
				System.out.println(axis_0.getMembers().get(0).getName());
				for (Position axis_1: cellSet.getAxes().get(Axis.ROWS.axisOrdinal()).getPositions())
				{
					System.out.print(cellSet.getCell(Arrays.asList(axis_0.getOrdinal(), axis_1.getOrdinal())).getFormattedValue());
					System.out.print(" | " );
				}System.out.println();
			}
		}catch (Exception e) 
		{
			System.out.println("Error while executing MDX query");
			e.printStackTrace();
		}
	}




	public static void main(String[] args) {
		// test pour une connexion à mondrian et l'execution d'une requete MDX 
		doFirst();
		doConnection("jdbc:mondrian:Jdbc=jdbc:postgresql://localhost:5432/warehouse?user=postgres&password=microlife; Catalog=file:tp2.xml; JdbcDrivers=org.postgresql.Driver");
		String mdx = "SELECT Non empty [Customer].[Name].Members ON COLUMNS, Non empty [Time].[Year].Members ON ROWS from [Sales_Cube]";
		displayQuery(mdx);

	}

}