import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

public class DataSet {

    private double[] x;
    private double[] y;

    public DataSet(String filePath, int sheetNum) {
        readDataFromExcel(filePath, sheetNum);
    }

    private void readDataFromExcel(String filePath, int sheetNum) {
        try (FileInputStream file = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(sheetNum);
            int rows = sheet.getPhysicalNumberOfRows();
            int cols = sheet.getRow(0).getPhysicalNumberOfCells();

            x = new double[rows - 1];
            y = new double[rows - 1];

            for (int i = 1; i < rows; i++) {
                Row row = sheet.getRow(i);

                x[i - 1] = row.getCell(0).getNumericCellValue();
                y[i - 1] = row.getCell(cols - 1).getNumericCellValue();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printDataSet() {
        System.out.println("X values:");
        for (double value : x) {
            System.out.print(value + "\t");
        }
        System.out.println();

        System.out.println("Y values:");
        for (double value : y) {
            System.out.print(value + "\t");
        }
        System.out.println();
    }

    public double[] getX() {
        return x;
    }

    public double[] getY() {
        return y;
    }
}