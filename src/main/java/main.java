import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import javax.imageio.metadata.IIOMetadata;
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

        //Selecionar onde salvar a planta convertida
        fileChooser.setDialogTitle("Selecionar local para salvar a planta em PNG");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //Busca apenas o diretório
        fileChooser.showOpenDialog(dialog);
        String diretorioPNG = fileChooser.getCurrentDirectory().toString();

        //Converte para Imagem e salva o local
        String plantaConvertida = convertPdfToImage(plantaPDF, diretorioPNG);

        //Selecionar o Carimbo
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); //Busca arquivos
        fileChooser.setDialogTitle("Selecionar carimbo em PNG");
        fileChooser.showOpenDialog(dialog);
        String carimbo = fileChooser.getSelectedFile().toString();

        //Selecionar onde salvar a planta carimbada
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Selecionar onde salvar a planta autenticada");
        fileChooser.showOpenDialog(dialog);
        String diretorio = fileChooser.getCurrentDirectory().toString();

        System.out.println(plantaPDF);
        System.out.println(diretorioPNG);
        System.out.println(carimbo);
        System.out.println(diretorio);
        System.out.println(mergeImages(plantaConvertida, carimbo, diretorio));
    }

    public static String convertPdfToImage(String sourceDir, String destinationDir) {
        File sourceFile = new File(sourceDir);
        try {
            if(sourceFile.exists()) {
                PDDocument document = PDDocument.load(sourceFile);
                PDFRenderer pdfRenderer = new PDFRenderer(document);

                String fileName = sourceFile.getName().replace(".pdf", "");
                BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
                String location = destinationDir + "/Downloads/" + fileName +".png";
                File outputfile = new File(location);
                ImageIO.write(image, "PNG", outputfile);
                document.close();
                return location;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "erro";
        }
        return "";
    }

    public static String mergeImages(String plantaDir, String carimboDir, String diretorio) {
        try {
            BufferedImage planta = ImageIO.read(new File(plantaDir));
            BufferedImage carimbo = ImageIO.read(new File(carimboDir));

            int width = planta.getWidth();
            int height = planta.getHeight();
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics graphics = bufferedImage.getGraphics();
            graphics.drawImage(planta, 0, 0, null);
            double widthCarimbo = (((((planta.getWidth() * 25.4f)/300)-185)*300)/25.4f);
            double heigthCarimbo = (((((planta.getHeight() * 25.4f)/300)-275)*300)/25.4f);
            graphics.drawImage(carimbo, (int) widthCarimbo, (int) heigthCarimbo, null);

            graphics.dispose();

            String location = diretorio+"/Downloads/"+"plantaautenticada.png";
            File outputfile = new File(location);
            ImageIO.write(bufferedImage, "PNG", outputfile);
            return location;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
