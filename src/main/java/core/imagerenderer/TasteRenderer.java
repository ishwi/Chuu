package core.imagerenderer;

import core.imagerenderer.util.fitter.StringFitter;
import core.imagerenderer.util.fitter.StringFitterBuilder;
import dao.entities.ResultWrapper;
import dao.entities.UserArtistComparison;
import dao.entities.UserInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TasteRenderer {
    private static final int X_MAX = 600;

    private static final int PROFILE_IMAGE_SIZE = 100;
    private static final int Y_MAX = 500;

    private TasteRenderer() {
    }


    public static BufferedImage generateTasteImage(ResultWrapper<UserArtistComparison> resultWrapper, List<UserInfo> userInfoLiust, String entityName, @Nullable String url, boolean thumbnail, Pair<Integer, Integer> tasteBar, @Nullable Pair<Color, Color> palette) {


        Color temp1;
        Color temp2;
        if (palette == null) {
            temp1 = Color.ORANGE;
            temp2 = Color.CYAN;
        } else {
            temp1 = palette.getLeft();
            temp2 = palette.getRight();
            if (temp1.equals(temp2)) {
                temp1 = temp2.brighter();
                temp2 = temp2.darker();
            }
        }
        float[] rgb1 = new float[3];
        temp1.getRGBColorComponents(rgb1);
        Color colorA = new Color(rgb1[0], rgb1[1], rgb1[2], 0.5f);
        Color colorA1 = new Color(rgb1[0], rgb1[1], rgb1[2], 0.8f);

        float[] rgb2 = new float[3];
        temp2.getRGBColorComponents(rgb2);
        Color colorB = new Color(rgb2[0], rgb2[1], rgb2[2], 0.5f);
        Color colorB1 = new Color(rgb2[0], rgb2[1], rgb2[2], 0.8f);
        BufferedImage canvas = new BufferedImage(X_MAX, Y_MAX, BufferedImage.TYPE_INT_RGB);

        List<BufferedImage> imageList = new ArrayList<>();

        Graphics2D g = canvas.createGraphics();
        GraphicUtils.setQuality(g);
        if (url != null) {
            BufferedImage imageFromUrl = GraphicUtils.getImageFromUrl(url, GraphicUtils.noArtistImage);
            GraphicUtils.initArtistBackground(canvas, imageFromUrl);
        } else {
            GraphicUtils.initRandomImageBlurredBackground(g, X_MAX, Y_MAX);
        }
        //Gets Profile Images
        for (UserInfo userInfo : userInfoLiust) {
            BufferedImage image = GraphicUtils.getImage(userInfo.getImage());
            if (image == null) {
                imageList.add(GraphicUtils.noArtistImage);
            } else {
                imageList.add(image);
            }
        }

        //Init Of Variables
        Font artistFont = new Font("Roboto", Font.PLAIN, 21);
        Font numberFont = new Font("Heebo Light", Font.PLAIN, 21);
        Font titleFont = new Font("Heebo Light", Font.PLAIN, 23);
        Font scrobbleFont = new Font("Heebo Light", Font.BOLD, 17);
        int startFont = 26;
        Font usernameFont = (new Font("Roboto Medium", Font.PLAIN, startFont));
        Font subtitle = new Font("Roboto Condensed Bold Italic", Font.ITALIC, 12);

        int x = 0;
        int y = 20;
        int image1StartPosition = 20;
        int image2StartPosition = canvas.getWidth() - PROFILE_IMAGE_SIZE - 20;
        g.setColor(Color.WHITE);
        int rectangleStartY = y + PROFILE_IMAGE_SIZE - 20;
        int rectangleHeight = g.getFontMetrics().getHeight();
        int rectangleWidth = image2StartPosition - image1StartPosition - PROFILE_IMAGE_SIZE - 8;


        g.setFont(usernameFont);
        String username = userInfoLiust.get(0).getUsername();
        String username1 = userInfoLiust.get(1).getUsername();
        int widht1 = g.getFontMetrics().stringWidth(username);
        int width2 = g.getFontMetrics().stringWidth(username1);
        int totalwidth = widht1 + width2 + 4;
        int disponibleSize = rectangleWidth + 8;

        while (totalwidth >= disponibleSize) {
            startFont -= 2;
            usernameFont = new Font("Roboto Medium", Font.PLAIN, startFont);
            g.setFont(usernameFont);
            widht1 = g.getFontMetrics().stringWidth(username);
            width2 = g.getFontMetrics().stringWidth(username1);
            totalwidth = widht1 + width2 + 4;
        }
        int totalCount = tasteBar.getRight() + tasteBar.getLeft();

        //Draws Profile Images
        for (BufferedImage image : imageList) {
            int drawx;
            int nameStringPosition;
            Color color;
            int countStringPosition;
            int rectanglePosition;
            UserInfo userInfo = userInfoLiust.get(x);
            float percentage;
            int plays;

            if (x == 0) {
                plays = tasteBar.getLeft();
                percentage = (float) tasteBar.getLeft() / totalCount;
                drawx = image1StartPosition;
                nameStringPosition = image1StartPosition + PROFILE_IMAGE_SIZE + 4;
                color = colorA.brighter();
                countStringPosition = image1StartPosition + PROFILE_IMAGE_SIZE + 5;
                rectanglePosition = countStringPosition - 1;
            } else {
                plays = tasteBar.getRight();

                percentage = (float) tasteBar.getRight() / totalCount;
                drawx = image2StartPosition;
                nameStringPosition = image2StartPosition - width2 - 4;
                color = colorB.brighter();
                countStringPosition = image2StartPosition - g.getFontMetrics()
                        .stringWidth(String.valueOf(tasteBar.getRight())) - 5;
                rectanglePosition = (int) (image2StartPosition - percentage * rectangleWidth) - 4;
            }
            g.setColor(color);
            g.drawImage(image, drawx, y, 100, 100, null);
            g.fillRect(rectanglePosition, rectangleStartY, (int) (rectangleWidth * percentage), rectangleHeight);
            g.setColor(Color.WHITE);
            g.setFont(usernameFont);
            GraphicUtils.drawStringNicely(g, userInfo
                    .getUsername(), nameStringPosition, 20 + PROFILE_IMAGE_SIZE / 2, canvas);
            g.setFont(scrobbleFont);
            GraphicUtils.drawStringNicely(g, "" + plays, countStringPosition, rectangleStartY + rectangleHeight - 1, canvas);
            x++;

        }

        //Draws Common Artists
        y = rectangleStartY + 64 + 20;
        String a = String.valueOf(resultWrapper.getRows());

        g.setFont(titleFont);
        int length = g.getFontMetrics().stringWidth(a);
        GraphicUtils.drawStringNicely(g, "" + resultWrapper.getRows(), X_MAX / 2 - length / 2, y - 30, canvas);

        g.setFont(subtitle);

        GraphicUtils.drawStringNicely(g, "common " + entityName, X_MAX / 2 + length / 2 + 4, y - 30, canvas);

        //Draws Top 10

        List<UserArtistComparison> resultList = resultWrapper.getResultList();
        for (int i = 0, resultListSize = resultList.size(); i < resultListSize && i < 10; i++) {
            UserArtistComparison item = resultList.get(i);


            String artistID = item.getArtistID();
            int countA = item.getCountA();
            int countB = item.getCountB();

            int halfDistance = X_MAX - 200;
            int ac = Math.round((float) countA / (float) (countA + countB) * halfDistance);
            int bc = Math.round((float) countB / (float) (countA + countB) * halfDistance);
            g.setColor(colorA1);
            g.fillRect(X_MAX / 2 - ac / 2, y + 3, ac / 2, 5);
            g.setColor(colorB1);
            g.fillRect(X_MAX / 2, y + 3, bc / 2, 5);

            g.setColor(Color.WHITE);
            g.setFont(numberFont);
            String strCountBString = String.valueOf(item.getCountB());

            int widthB = g.getFontMetrics().stringWidth(strCountBString);

            int countBStart = X_MAX - 100 - widthB;
            if (thumbnail) {
                BufferedImage resized = GraphicUtils.resizeOrCrop(GraphicUtils.getImageFromUrl(item.getUrl(), GraphicUtils.noArtistImage), 30);
                g.drawImage(resized, 35, y - resized.getHeight() / 2 - 5, 30, 30, null);
                g.drawImage(resized, X_MAX - 35 - 30, y - resized.getHeight() / 2 - 5, 30, 30, null);

            }

            GraphicUtils.drawStringNicely(g, "" + countA, 100, y, canvas);
            GraphicUtils.drawStringNicely(g, "" + countB, countBStart, y, canvas);
            Font ogFont = g.getFont();


            StringFitter.FontMetadata artistMetadata = new StringFitterBuilder(21, X_MAX - 200 - widthB * 2)
                    .setBaseFont(artistFont)
                    .setMinSize(14).build()
                    .getFontMetadata(g, artistID);

            GraphicUtils.drawStringNicely(g, artistMetadata, (int) (X_MAX / 2 - (artistMetadata.bounds().getWidth() / 2)), y, canvas);
            g.setFont(ogFont);
            y += 32;
        }
        g.dispose();
        return canvas;
    }


}
