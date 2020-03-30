package com.compomics.main;

import com.compomics.ms2io.controller.Indexer;
import com.compomics.ms2io.controller.MgfWriter;
import com.compomics.ms2io.controller.MspReader;
import com.compomics.ms2io.controller.SpectraReader;
import com.compomics.ms2io.controller.SpectraWriter;
import com.compomics.ms2io.model.IndexKey;
import com.compomics.ms2io.model.Spectrum;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author Genet
 */
public class TFDR {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, InvalidFormatException {

        File file = new File("C:\\human_hcd\\lib\\MassIVE\\GeneratedSpectraLib_Annotated\\MassIVE_synthetic_10p_sharedSpecs_random.msp");
        Indexer giExp = new Indexer(file);
        List<IndexKey>indxList = giExp.generate();
        SpectraReader rd = new MspReader(file, indxList);
       // SpectraWriter rw = new MgfWriter(new File("C:\\human_hcd\\lib\\MassIVE\\GeneratedSpectraLib_Annotated\\MassIVE_synthetic_10p_sharedSpecs_random.mgf"));
    
       String file_name = "C:\\human_hcd\\lib\\MassIVE\\SearchResults\\COSS_MsRobin\\validated\\validatedMassIVE_syntheticTest_20p_PCSwap_MsRobin";
        Spectrum spec=null;
        FileInputStream ip = new FileInputStream(file_name + ".xlsx");
 
        Workbook wb = WorkbookFactory.create(ip);
        Sheet sheet = wb.getSheet("Sheet1");
        List<String> peptides = new ArrayList<>();
        for(IndexKey k : indxList){
            spec = rd.readAt(k.getPos());
            peptides.add(spec.getTitle());
            
        }
        DataFormatter df = new DataFormatter();
        Iterator<Row> rowIterator = sheet.rowIterator();
       
         Row row = rowIterator.next();
        while (rowIterator.hasNext()) {
            row = rowIterator.next();
            Cell cell_title = row.getCell(0);
            String title = df.formatCellValue(cell_title);
            if(peptides.contains(title)){
                Cell cell = row.getCell(15);
                if(cell == null){
                    cell=row.createCell(15);
                    cell.setCellType(CellType.NUMERIC);
                    cell.setCellValue(1);
                }
            }else{
                Cell cell = row.getCell(15);
                if(cell == null){
                    cell=row.createCell(15);
                    cell.setCellType(CellType.STRING);
                    cell.setCellValue(0);
                }
            }  
        }

      FileOutputStream outFile = new FileOutputStream(new File(file_name + "_apended.xlsx"));
      wb.write(outFile);
      outFile.close();
        ip.close();
    }
}
