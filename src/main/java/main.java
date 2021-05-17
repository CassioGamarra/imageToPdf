import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;

public class main {
    public static void main(String[] args) {
        JDialog dialog = new JDialog(); //Cria um Dialog padrão
        JFileChooser fileChooser = new JFileChooser(); //Permite selecionar arquivos
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); //Busca arquivos
        //Selecionar PDF
        fileChooser.setDialogTitle("Selecionar planta em PDF");
        fileChooser.showOpenDialog(dialog);
        String plantaPDF = fileChooser.getSelectedFile().toString();

        //Converte para Imagem e salva o local
        String plantaConvertida = convertPdfToBase64Image(plantaPDF);

        //Selecionar o Carimbo
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); //Busca arquivos
        fileChooser.setDialogTitle("Selecionar carimbo em PNG");
        fileChooser.showOpenDialog(dialog);
        String carimbo = fileChooser.getSelectedFile().toString();
        String carimboB64 = converteImageToBase64(carimbo);

        String plantaAutenticada = mergeImagesToPDFBase64(plantaConvertida, carimboB64);
        //String pdfOriginal = convertPDFTobase64(plantaPDF);
        //String result = insertImageIntoPDF(plantaAutenticada, pdfOriginal);
        saveToTxt(plantaAutenticada);
    }

    public static void saveToTxt(String value) {
        try {
            FileWriter fileWriter  = new FileWriter("/home/jhonatan/Downloads/plantacarimbada.txt");
            fileWriter.write(value);
            fileWriter.close();
            System.out.println("Arquivo salvo com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String convertPdfToBase64Image(String sourceDir) {
        File sourceFile = new File(sourceDir);
        try {
            if(sourceFile.exists()) {
                PDDocument pdDocument = PDDocument.load(sourceFile);
                PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "PNG", outputStream);
                pdDocument.close();
                return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "erro";
        }
        return "";
    }

    public static String mergeImagesToPDFBase64(String plantaB64, String carimboB64) {
        try {
            byte[] imageBytes;

            imageBytes = Base64.getDecoder().decode(plantaB64);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage planta = ImageIO.read(byteArrayInputStream);

            imageBytes = Base64.getDecoder().decode(carimboB64);
            byteArrayInputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage carimbo = ImageIO.read(byteArrayInputStream);

            int width = planta.getWidth();
            int height = planta.getHeight();
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics graphics = bufferedImage.getGraphics();
            graphics.drawImage(planta, 0, 0, null);
            double widthCarimbo = (((((planta.getWidth() * 25.4f)/300)-185)*300)/25.4f);
            double heigthCarimbo = (((((planta.getHeight() * 25.4f)/300)-275)*300)/25.4f);
            graphics.drawImage(carimbo, (int) widthCarimbo, (int) heigthCarimbo, null);

            graphics.dispose();

            ByteArrayOutputStream outputImageStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", outputImageStream);

            return Base64.getEncoder().encodeToString(outputImageStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String insertImageIntoPDF(String plantaCarimbada, String pdfOriginal) {
        byte[] imageBytes, pdfBytes;
        imageBytes = Base64.getDecoder().decode(plantaCarimbada);
        pdfBytes = Base64.getDecoder().decode(pdfOriginal);
        try {
            PDDocument document = PDDocument.load(pdfBytes);
            PDPage page = document.getPage(0);
            PDImageXObject pdImageXObject = PDImageXObject.createFromByteArray(document, imageBytes, "Planta Autenticada");
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.drawImage(pdImageXObject, pdImageXObject.getWidth(), pdImageXObject.getHeight());
            contentStream.close();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            document.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String convertPDFTobase64(String pdfDir) {
        try {
            byte[] inFileBytes = Files.readAllBytes(Paths.get(pdfDir));
            return Base64.getEncoder().encodeToString(inFileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String converteImageToBase64(String imageDir) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(imageDir));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
