package core.imagerenderer.util;

import static java.lang.Math.*;

public class D {

    public static double getTheFreakingDistance(float[] first, float[] second) {
        float CIE_L_1 = first[0], CIE_a_1 = first[1], CIE_b_1 = first[2];          //Color #1 CIE-L*ab value s
        float CIE_L_2 = second[0], CIE_a_2 = second[1], CIE_b_2 = second[2];         //Color #2 CIE-L*ab values
        double WHT_L = 1, WHT_C = 1, WHT_H = 1;                //Wheight factors

        double xC1 = sqrt(CIE_a_1 * CIE_a_1 + CIE_b_1 * CIE_b_1);
        double xC2 = sqrt(CIE_a_2 * CIE_a_2 + CIE_b_2 * CIE_b_2);
        double xCX = (xC1 + xC2) / 2;
        double xGX = 0.5 * (1 - sqrt((pow(xCX, 7)) / ((pow(xCX, 7)) + (pow(25, 7)))));
        double xNN = (1 + xGX) * CIE_a_1;

        xC1 = sqrt(xNN * xNN + CIE_b_1 * CIE_b_1);

        double xH1 = CieLab2Hue(xNN, CIE_b_1);
        xNN = (1 + xGX) * CIE_a_2;
        xC2 = sqrt(xNN * xNN + CIE_b_2 * CIE_b_2);
        double xH2 = CieLab2Hue(xNN, CIE_b_2);
        double xDL = CIE_L_2 - CIE_L_1;
        double xDC = xC2 - xC1;
        double xDH;
        double xHX;
        if ((xC1 * xC2) == 0) {
            xDH = 0;
        } else {
            xNN = round(xH2 - xH1);
            if (abs(xNN) <= 180) {
                xDH = xH2 - xH1;
            } else {
                if (xNN > 180) xDH = xH2 - xH1 - 360;
                else xDH = xH2 - xH1 + 360;
            }
        }

        xDH = 2 *

              sqrt(xC1 * xC2) *

              sin(Math.toRadians(xDH / 2));
        double xLX = (CIE_L_1 + CIE_L_2) / 2;
        double xCY = (xC1 + xC2) / 2;
        if ((xC1 * xC2) == 0) {
            xHX = xH1 + xH2;
        } else {
            xNN = abs(round(xH1 - xH2));
            if (xNN > 180) {
                if ((xH2 + xH1) < 360) xHX = xH1 + xH2 + 360;
                else xHX = xH1 + xH2 - 360;
            } else {
                xHX = xH1 + xH2;
            }
            xHX /= 2;
        }

        double xTX = 1 - 0.17 * cos(Math.toRadians(xHX - 30)) + 0.24
                                                                * cos(Math.toDegrees(2 * xHX)) + 0.32
                                                                                                 * cos(Math.toDegrees(3 * xHX + 6)) - 0.20
                                                                                                                                      * cos(Math.toRadians(4 * xHX - 63));
        double xPH = 30 * exp(-((xHX - 275) / 25) * ((xHX - 275) / 25));
        double xRC = 2 * sqrt((pow(xCY, 7)) / ((pow(xCY, 7)) + (25 ^ 7)));
        double xSL = 1 + ((0.015 * ((xLX - 50) * (xLX - 50)))
                          / sqrt(20 + ((xLX - 50) * (xLX - 50))));

        double xSC = 1 + 0.045 * xCY;
        double xSH = 1 + 0.015 * xCY * xTX;
        double xRT = -sin(Math.toRadians(2 * xPH)) * xRC;
        xDL = xDL / (WHT_L * xSL);
        xDC = xDC / (WHT_C * xSC);
        xDH = xDH / (WHT_H * xSH);

        return sqrt(pow(xDL, 2) + pow(xDC, 2) + pow(xDH, 2) + xRT * xDC * xDH);
    }


    public static double CieLab2Hue(double var_a, double var_b)          //Function returns CIE-H° value
    {
        float var_bias = 0;
        if (var_a >= 0 && var_b == 0) return 0;
        if (var_a < 0 && var_b == 0) return 180;
        if (var_a == 0 && var_b > 0) return 90;
        if (var_a == 0 && var_b < 0) return 270;
        if (var_a > 0 && var_b > 0) var_bias = 0;
        if (var_a < 0) var_bias = 180;
        if (var_a > 0 && var_b < 0) var_bias = 360;
        return (Math.toDegrees(atan(var_b / var_a)) + var_bias);
    }

    public static double doSomethin2(float[] first, float[] second) {
        float CIE_L_1 = first[0], CIE_a_1 = first[1], CIE_b_1 = first[2];          //Color #1 CIE-L*ab value s
        float CIE_L_2 = second[0], CIE_a_2 = second[1], CIE_b_2 = second[2];         //Color #2 CIE-L*ab values
        double WHT_L = 1, WHT_C = 1, WHT_H = 1;                //Wheight factors


        double xC1 = sqrt((pow(CIE_a_1, 2)) + (pow(CIE_b_1, 2)));
        double xC2 = sqrt((pow(CIE_a_2, 2)) + (pow(CIE_b_2, 2)));
        double xDL = CIE_L_2 - CIE_L_1;
        double xDC = xC2 - xC1;
        double xDE = sqrt(((CIE_L_1 - CIE_L_2) * (CIE_L_1 - CIE_L_2))
                          + ((CIE_a_1 - CIE_a_2) * (CIE_a_1 - CIE_a_2))
                          + ((CIE_b_1 - CIE_b_2) * (CIE_b_1 - CIE_b_2)));

        double xDH = (xDE * xDE) - (xDL * xDL) - (xDC * xDC);
        if (xDH > 0) {
            xDH = sqrt(xDH);
        } else {
            xDH = 0;
        }
        double xSC = 1 + (0.045 * xC1);
        double xSH = 1 + (0.015 * xC1);
        xDL /= WHT_L;
        xDC /= WHT_C * xSC;
        xDH /= WHT_H * xSH;

        return sqrt(pow(xDL, 2) + pow(xDC, 2) + (pow(xDH, 2)));
    }

}
