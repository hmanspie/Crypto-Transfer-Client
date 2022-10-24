package com.rebalcomb.crypto;

import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class Hiding implements IHiding {
    private static final String OUTPUT_FILE_PATH = "outpng.png";
    private String indicator = "A";

    public void methodCheek() {
        IHiding hiding = new Hiding();

        StringBuilder startMassage = new StringBuilder();
        for (int i = 0; i < 515; i++) {
            startMassage.append("1");
        }
        System.out.println(startMassage.length());
        System.out.println(startMassage);
        List<String> encodedString = hiding.generateHidingMassage(startMassage.toString());

        int firstHash = startMassage.toString().hashCode();

        List<String> big = addRedundantPictures(encodedString, 3);

        System.out.println("Length: " + big.size());
        String massage = hiding.getOpenMassageForHidingMassage(big);
        //String massage = hiding.getOpenMassageForHidingMassage(encodedString);
        System.out.println(massage);
        System.out.println(massage.length());
        System.out.println("Result: " + (firstHash == massage.hashCode()));
    }

    private String steganography(String imageForBase64, String msg) {

        BufferedImage image = convertToPNG(imageForBase64);
        int w = image.getWidth();
        int h = image.getHeight();

        byte[] msgbytes = msg.getBytes();

        int msglendecode = (image.getRGB(0, 0) >> 8) << 8;

        msglendecode |= msg.length();
        image.setRGB(0, 0, msglendecode);//hidig msg length at first position

        for (int i = 1, msgpos = 0, row = 0, j = 0; row < h; row++) {
            for (int col = 0; col < w && j < msgbytes.length; col++, i++) {

                if (i % 11 == 0) {

                    int rgb = image.getRGB(col, row);

                    int a = ((rgb >> 24) & 0xff);

                    int r = (((rgb >> 16) & 0xff) >> 3) << 3;
                    r = r | (msgbytes[msgpos] >> 5);

                    int g = (((rgb >> 8) & 0xff) >> 3) << 3;
                    g = g | ((msgbytes[msgpos] >> 2) & 7);

                    int b = ((rgb & 0xff) >> 2) << 2;
                    b = b | (msgbytes[msgpos] & 0x3);


                    rgb = 0;
                    rgb = (rgb | (a << 24));
                    rgb = (rgb | (r << 16));
                    rgb = (rgb | (g << 8));

                    rgb = (rgb | b);

                    image.setRGB(col, row, rgb);

                    msgpos++;
                    j++;
                }
            }
        }
        File outputFile = new File(OUTPUT_FILE_PATH);
        try {
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            System.out.println("error in saving image ");
        }

        return convertToBase64();
        //return image;
    }

    private String decodeSteganography(String encodedString) {
        BufferedImage bimg = convertToPNG(encodedString);
        int w = bimg.getWidth(), h = bimg.getHeight();
        int msglength = (bimg.getRGB(0, 0) & 0xff);

        StringBuilder massage = new StringBuilder();
        for (int row = 0, j = 0, i = 1; row < h; row++) {
            for (int col = 0; col < w && j < msglength; col++, i++) {

                if (i % 11 == 0) {
                    int result = bimg.getRGB(col, row);

                    int charatpos = (((result >> 16) & 0x7) << 5);

                    charatpos |= (((result >> 8) & 0x7) << 2);

                    charatpos |= ((result & 0x3));

                    massage.append((char) charatpos);
                    j++;
                }
            }
        }
        return massage.toString();
    }

    private String convertToBase64() {
        byte[] fileContent;
        try {
            fileContent = FileUtils.readFileToByteArray(new File(OUTPUT_FILE_PATH));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        deleteFile();
        return Base64.getEncoder().encodeToString(fileContent);
    }

    private BufferedImage convertToPNG(String encodedString) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        BufferedImage image;
        File path = new File(OUTPUT_FILE_PATH);
        try {
            FileUtils.writeByteArrayToFile(path, decodedBytes);
            image = ImageIO.read(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        deleteFile();
        return image;
    }

    private void deleteFile() {
        File file = new File(OUTPUT_FILE_PATH);
        System.out.println("Result for delete: " + file.delete());
    }

    private ArrayList<String> divideIntoParts(String rawMassage) {
        int partLength = 253;

        ArrayList<String> massage = new ArrayList<>();
        int remainder = rawMassage.length() % partLength;
        int wholePart = rawMassage.length() / partLength;
        for (int i = 0; i < wholePart + 1; i++) {
            if (i * partLength + remainder == rawMassage.length()) {
                massage.add(rawMassage.substring(wholePart * partLength));
                break;
            }
            int j = i * partLength;
            massage.add(rawMassage.substring(j, j + partLength));
        }
        massage.removeIf(x -> x.length() == 0);

        for (int i = 0; i < massage.size(); i++) {
            massage.set(i, indicator + i + massage.get(i));
        }

        return massage;
    }

    public List<String> generateHidingMassage(String rawMassage) {
        ArrayList<String> massage = divideIntoParts(rawMassage);
        String[] hiddenMassages = new String[massage.size()];
        String[] massImage = getImageForDatabase(massage.size());

        for (int i = 0; i < massage.size(); i++) {
            hiddenMassages[i] = steganography(massImage[i], massage.get(i));
        }
        return Arrays.stream(hiddenMassages).toList();
    }

    public String getOpenMassageForHidingMassage(List<String> hidingMassage) {
        StringBuilder openMassage = new StringBuilder();
        ArrayList<String> redundantMassage = new ArrayList<>();

        for (String massage : hidingMassage) {
            String decodeMassage = decodeSteganography(massage);
            String numberPart = decodeMassage.substring(1, 2);
            String indicatorForMassage = decodeMassage.substring(0, 1);

            if (checkNumber(numberPart) && indicatorForMassage.equals(indicator)) {
                redundantMassage.add(decodeMassage);
            }
        }
        Collections.sort(redundantMassage);

        for (String s : redundantMassage) {
            openMassage.append(s.substring(2));
        }

        return openMassage.toString();
    }

    private boolean checkNumber(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    public List<String> addRedundantPictures(List<String> hidingMassage, int count) {
        indicator = "B";
        StringBuilder startMassage = new StringBuilder();
        for (int i = 0; i < count * 250; i++) {
            startMassage.append("l");
        }
        List<String> additionalLoad = generateHidingMassage(startMassage.toString());
        hidingMassage = Stream.of(hidingMassage, additionalLoad).flatMap(Collection::stream).toList();
        int[] order = generateRandomOrder(hidingMassage.size());
        String[] redundantMassage = new String[hidingMassage.size()];

        for (int i = 0; i < hidingMassage.size(); i++) {
            redundantMassage[i] = hidingMassage.get(order[i]);
        }
        indicator = "A";
        return List.of(redundantMassage);
    }

    private int[] generateRandomOrder(int length) {
        Random random = new Random();
        int[] resultOrder = new int[length];
        Arrays.fill(resultOrder, -1);
        for (int i = 0; i < length; i++) {
            int randomNumber = random.nextInt(length);
            boolean repetition = false;
            for (int j = 0; j < length; j++) {
                if (resultOrder[j] == randomNumber) {
                    repetition = true;
                    break;
                }
            }
            if (!repetition) {
                resultOrder[i] = randomNumber;
            } else {
                i--;
            }
        }
        return resultOrder;
    }

    private String[] getImageForDatabase(int count) {


        return TEST();
    } //TODO: create method which get image for database

    private String[] TEST() {
        String[] result = new String[3];
        String[] path = new String[]{"PicturesForMassage\\18.png", "PicturesForMassage\\19.png", "PicturesForMassage\\20.png"};
        for (int i = 0; i < result.length; i++) {
            result[i] = ToBase64(path[i]);
        }
        return result;

    }//TODO delete this method

    //generate format for Database
    private String ToBase64(String path) {
        byte[] fileContent;
        try {
            fileContent = FileUtils.readFileToByteArray(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        deleteFile();
        return Base64.getEncoder().encodeToString(fileContent);
    }
}
