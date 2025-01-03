package mond;


import mondrian.olap.Connection;
import mondrian.olap.DriverManager;
import mondrian.olap.Result;
import mondrian.olap.Query;

import java.io.FileWriter;
import java.io.IOException;

public class MondrianExample {
    public static void main(String[] args) {
        String mondrianConnectionString =
                "Provider=mondrian;" +
                "Jdbc=jdbc:postgresql://localhost:5432/warehouse?user=postgres&password=microlife;" +
                "Catalog=file:tp2.xml;" +
                "JdbcDrivers=org.postgresql.Driver";

        try {
            // Establish Mondrian connection
            Connection connection = DriverManager.getConnection(mondrianConnectionString, null);

            // Define the MDX query
			String mdxQuery = "select NON EMPTY [Time].[Month].Members ON COLUMNS, NON EMPTY [Product].FirstSibling ON ROWS  from [Sales_Cube]";

            // Execute the query
            Query query = connection.parseQuery(mdxQuery);
            Result result = connection.execute(query);

            // Save the result to a CSV file
            saveResultToCsv(result, "output.csv");

            System.out.println("MDX query executed and results saved to output.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveResultToCsv(Result result, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Get the axes (columns and rows)
            mondrian.olap.Axis[] axes = result.getAxes();
            
            // Write headers based on the axes (columns and rows)
            StringBuilder header = new StringBuilder();
            for (int axisIdx = 0; axisIdx < axes.length; axisIdx++) {
                for (int positionIdx = 0; positionIdx < axes[axisIdx].getPositions().size(); positionIdx++) {
                    for (int memberIdx = 0; memberIdx < axes[axisIdx].getPositions().get(positionIdx).size(); memberIdx++) {
                        header.append(axes[axisIdx].getPositions().get(positionIdx).get(memberIdx).getName()).append(" | ");
                    }
                }
            }
            writer.write(header.toString().trim() + "\n");

            // Write data for each cell
            for (int rowIdx = 0; rowIdx < result.getAxes()[1].getPositions().size(); rowIdx++) {
                StringBuilder row = new StringBuilder();

                // Write row data from the row axis
                for (int rowMemberIdx = 0; rowMemberIdx < result.getAxes()[1].getPositions().get(rowIdx).size(); rowMemberIdx++) {
                    row.append(result.getAxes()[1].getPositions().get(rowIdx).get(rowMemberIdx).getName()).append(" | ");
                }

                // Write cell data for the intersection of row and column
                for (int colIdx = 0; colIdx < result.getAxes()[0].getPositions().size(); colIdx++) {
                    // Get the cell value using an array of indices (column index and row index)
                    String cellValue = result.getCell(new int[] {colIdx, rowIdx}).getFormattedValue();
                    row.append(cellValue).append(" , ");
                }

                // Remove last pipe and space, then write the row
                writer.write(row.toString().trim() + "\n");
            }
        }
    }

}
