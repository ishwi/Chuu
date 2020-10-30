package dao.entities;

public class RymStats {
    private int numberOfRatings;
    private double average;
    private int numberOf05;
    private int numberOf1;
    private int numberOf15;
    private int numberOf2;
    private int numberOf25;
    private int numberOf3;
    private int numberOf35;
    private int numberOf4;
    private int numberOf45;
    private int numberOf5;

    public RymStats(int numberOfRatings, double average, int numberOf05, int numberOf1, int numberOf15, int numberOf2, int numberOf25, int numberOf3, int numberOf35, int numberOf4, int numberOf45, int numberOf5) {
        this.numberOfRatings = numberOfRatings;
        this.average = average;
        this.numberOf05 = numberOf05;
        this.numberOf1 = numberOf1;
        this.numberOf15 = numberOf15;
        this.numberOf2 = numberOf2;
        this.numberOf25 = numberOf25;
        this.numberOf3 = numberOf3;
        this.numberOf35 = numberOf35;
        this.numberOf4 = numberOf4;
        this.numberOf45 = numberOf45;
        this.numberOf5 = numberOf5;
    }

    public RymStats(int numberOfRatings, double average) {
        this.numberOfRatings = numberOfRatings;
        this.average = average;
    }

    public int getNumberOfRatings() {
        return numberOfRatings;
    }

    public void setNumberOfRatings(int numberOfRatings) {
        this.numberOfRatings = numberOfRatings;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public int getNumberOf05() {
        return numberOf05;
    }

    public void setNumberOf05(int numberOf05) {
        this.numberOf05 = numberOf05;
    }

    public int getNumberOf1() {
        return numberOf1;
    }

    public void setNumberOf1(int numberOf1) {
        this.numberOf1 = numberOf1;
    }

    public int getNumberOf15() {
        return numberOf15;
    }

    public void setNumberOf15(int numberOf15) {
        this.numberOf15 = numberOf15;
    }

    public int getNumberOf2() {
        return numberOf2;
    }

    public void setNumberOf2(int numberOf2) {
        this.numberOf2 = numberOf2;
    }

    public int getNumberOf25() {
        return numberOf25;
    }

    public void setNumberOf25(int numberOf25) {
        this.numberOf25 = numberOf25;
    }

    public int getNumberOf3() {
        return numberOf3;
    }

    public void setNumberOf3(int numberOf3) {
        this.numberOf3 = numberOf3;
    }

    public int getNumberOf35() {
        return numberOf35;
    }

    public void setNumberOf35(int numberOf35) {
        this.numberOf35 = numberOf35;
    }

    public int getNumberOf4() {
        return numberOf4;
    }

    public void setNumberOf4(int numberOf4) {
        this.numberOf4 = numberOf4;
    }

    public int getNumberOf45() {
        return numberOf45;
    }

    public void setNumberOf45(int numberOf45) {
        this.numberOf45 = numberOf45;
    }

    public int getNumberOf5() {
        return numberOf5;
    }

    public void setNumberOf5(int numberOf5) {
        this.numberOf5 = numberOf5;
    }
}
