package com.rebalcomb.crypto;

import com.rebalcomb.service.MessageService;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class Hiding implements IHiding {
    /**
     * This is the name of the intermediate file in which the image sent from the server in Base64 format is stored,
     * for its further reading in the format BufferedImage
     */
    private static final String OUTPUT_FILE_PATH = "outpng.png";

    /**
     * This indicator is used to indicate the main message and its follow-up
     * assembly (if it was divided into several images)
     */
    private String indicator = "A";
    /**
     * It is used to indicate the transition between images when the deadline for submission is formed
     * to the server, or reverse splitting into separate images on the client side
     */
    private final String SEPARATOR = "###@###";

    /**
     * Converts the image type from the Base64 format to the BufferedImage class using the convertToPNG() method
     * and hides the message in the card using the LSB method
     * @param imageForBase64 - Picture in format Base64
     * @param msg - Message length <= 255 characters
     * @return A picture with a hidden message in Base64 format
     */
    private String steganography(String imageForBase64, String msg) {

        BufferedImage image = convertToPNG(imageForBase64);
        int w = image.getWidth();
        int h = image.getHeight();

        byte[] msgbytes = msg.getBytes();

        int msglendecode = (image.getRGB(0, 0) >> 8) << 8;

        msglendecode |= msg.length();
        image.setRGB(0, 0, msglendecode);

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
    }

    /**
     * Converts the image type from the Base64 format to the BufferedImage class using the convertToPNG() method
     * and retrieves the message hidden by the LSB method
     * @param encodedString - A picture with a hidden message in Base64 format
     * @return The message that was hidden in the picture
     */
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

    /**
     * Reads an image named OUTPUT_FILE_PATH and converts it to Base64 format
     * @return Picture in format Base64
     */
    private String convertToBase64() {
        byte[] fileContent;
        try {
            fileContent = FileUtils.readFileToByteArray(new File(OUTPUT_FILE_PATH));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }

    /**
     * Decodes the image from Base64 format, then writes it to a file named OUTPUT_FILE_PATH,
     * and reads in BufferedImage format
     * @param encodedString - Picture in format Base64
     * @return Picture in format BufferedImage
     */
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
        return image;
    }

    /**
     * Accepts an unprocessed message and splits it into 253 character parts
     * adding an indicator with the value A (this is an indicator of the main message), as well as
     * adds a number to the indicator that indicates the order of the message parts
     * @param rawMassage - The message that needs to be prepared for hiding in the picture
     * @return ArrayList<String> which is used when hiding a message
     */
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

    /**
     * After receiving the message, it calls the divideIntoParts(rawMassage) method to prepare the message
     * then the MessageService.getRandomImageList() method receives the required number of pictures and through a loop
     * hide all parts of the message in pictures and write pictures in Base64 format in 1 line via SEPARATOR
     * @param rawMassage - Unprocessed message
     * @return A line in which pictures with a hidden message are recorded through a SEPARATOR
     */
    public String generateHidingMassage(String rawMassage) {
        List<String> massage = divideIntoParts(rawMassage);
        String[] hiddenMassages = new String[massage.size()];
        List<String> massImage;
        try {
            massImage = MessageService.getRandomImageList(massage.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < massage.size(); i++) {
            hiddenMassages[i] = steganography(massImage.get(i), massage.get(i));
        }

        StringBuilder resultMassage = new StringBuilder();
        for (String part : hiddenMassages) {
            resultMassage.append(part).append(SEPARATOR);
        }
        return resultMassage.toString();
    }

    /**
     * Divides into an array of pictures and checks for an indicator, if A + a number, then saves to ArrayList<String>,
     * then sorts, then iterate through each Arraylist<String> object and extract messages from each picture
     * without the first 2 characters (indicator + part number)
     * @param hidingMassage - The line in which the pictures with the hidden message are recorded are separated by SEPARATOR
     * @return The message is hidden in the pictures
     */
    public String getOpenMassageForHidingMassage(String hidingMassage) {
        StringBuilder openMassage = new StringBuilder();
        ArrayList<String> redundantMassage = new ArrayList<>();

        for (String massage : hidingMassage.split(SEPARATOR)) {
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

    /**
     * Tests whether a string is a character
     * @param number - A string that is checked to see if it is a number
     * @return true, if string is a number otherwise false
     */
    private boolean checkNumber(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    /**
     * Adds redundant images to the finished message and mixes them together.
     * Messages in redundant pictures have a B indicator
     * @param hidingMassage - The result returned by the generateHidingMassage() method
     * @param count - The number of redundant pictures that will be added to the main message
     * @return Messages with redundant images
     */
    public String addRedundantPictures(String hidingMassage, int count) {
        List<String> hidingMassageParts = Arrays.stream(hidingMassage.split(SEPARATOR)).toList();
        indicator = "B";
        List<String> additionalLoad = Arrays.stream(generateHidingMassage("l".repeat(Math.max(0, count * 250))).split(SEPARATOR)).toList();
        hidingMassageParts = Stream.of(hidingMassageParts, additionalLoad).flatMap(Collection::stream).toList();
        int[] order = generateRandomOrder(hidingMassageParts.size());
        String[] redundantMassage = new String[hidingMassageParts.size()];

        for (int i = 0; i < hidingMassageParts.size(); i++) {
            redundantMassage[i] = hidingMassageParts.get(order[i]);
        }

        StringBuilder resultMassage = new StringBuilder();
        for (String part: redundantMassage) {
            resultMassage.append(part).append(SEPARATOR);
        }
        indicator = "A";
        return resultMassage.toString();
    }

    /**
     * The method randomly generates a sequence of numbers of a given length
     * @param length - Number of numbers to be generated from 0 to length key
     * @return int[]
     */
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
}
/**
 * Steganography techniques were taken from this site https://stackoverflow.com/questions/16643495/hiding-message-in-jpg-image
 */
