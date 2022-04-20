package com.yuliya1303;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class TestFiles {
    ClassLoader classLoader = TestFiles.class.getClassLoader();

    @Test
    void testDownloadFile() throws Exception {
        open("https://github.com/junit-team/junit5/blob/main/README.md");
        File textFile = $("#raw-url").download();
        try (InputStream stream = new FileInputStream(textFile)) {
            byte[] fileContent = stream.readAllBytes();
            String content = new String(fileContent, StandardCharsets.UTF_8);
            Assertions.assertThat(content).contains("JUnit 5");
        }
    }

    @Test
    void zipParsingPDFTest() throws Exception {
        ZipFile zf = new ZipFile(new File("src/test/resources/PDF.zip"));
        try (ZipInputStream is = new ZipInputStream(classLoader.getResourceAsStream("PDF.zip"))) {
            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                org.assertj.core.api.Assertions.assertThat(entry.getName()).isEqualTo("208.pdf");
                try (InputStream inputStream = zf.getInputStream(entry)) {
                    PDF pdf = new PDF(inputStream);
                    Assertions.assertThat(pdf.numberOfPages).isEqualTo(598);
                    Assertions.assertThat(pdf.creator).isEqualTo("convertonlinefree.com");
                }
            }
        }
    }

    @Test
    void zipParsingPngTest() throws Exception {
        ZipFile zf = new ZipFile(new File("src/test/resources/Image.zip"));
        try (ZipInputStream is = new ZipInputStream(classLoader.getResourceAsStream("Image.zip"))) {
            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                org.assertj.core.api.Assertions.assertThat(entry.getName()).isEqualTo("fish.png");
                try (InputStream inputStream = zf.getInputStream(entry)) {
                    BufferedImage img = ImageIO.read(inputStream);
                    Assertions.assertThat(img.getHeight()).isEqualTo(311);
                    Assertions.assertThat(img.getWidth()).isEqualTo(800);
                }
            }
        }
    }

    @Test
    void zipParsingXlsxTest() throws Exception {
        ZipFile zf = new ZipFile(new File("src/test/resources/Excel.zip"));
        try (ZipInputStream is = new ZipInputStream(classLoader.getResourceAsStream("Excel.zip"))) {
            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                org.assertj.core.api.Assertions.assertThat(entry.getName()).isEqualTo("Some_table.xlsx");
                try (InputStream inputStream = zf.getInputStream(entry)) {
                    XLS xls = new XLS(inputStream);
                    String stringCellValue = xls.excel.getSheetAt(0).getRow(2).getCell(1).getStringCellValue();
                    org.assertj.core.api.Assertions.assertThat(stringCellValue).contains("Levkovets");
                    int header = xls.excel.getSheetAt(0).getLastRowNum();
                    org.assertj.core.api.Assertions.assertThat(header).isEqualTo(2);
                }
            }
        }
    }

    @Test
    void zipParsingCsvTest() throws Exception {
        ZipFile zf = new ZipFile(new File("src/test/resources/CSV.zip"));
        try (ZipInputStream is = new ZipInputStream(classLoader.getResourceAsStream("CSV.zip"))) {
            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                org.assertj.core.api.Assertions.assertThat(entry.getName()).isEqualTo("Machine_readable_file_bdc_sf_2021_q4.csv");
                try (InputStream inputStream = zf.getInputStream(entry)) {
                    try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        List<String[]> content = reader.readAll();
                        Assertions.assertThat(content).contains(
                                new String[] {"Series_reference","Period","Data_value","Suppressed","STATUS","UNITS","Magnitude","Subject",
                                        "Group","Series_title_1","Series_title_2","Series_title_3","Series_title_4","Series_title_5"}
                        );
                    }
                }
            }
        }
    }
}
