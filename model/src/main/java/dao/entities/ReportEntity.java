package dao.entities;

import java.time.LocalDateTime;

public class ReportEntity {
    private final String url;
    private final long whoGotReported;
    private final long artistReported;
    private final long imageReported;
    private final LocalDateTime firstReport;
    private final String artistName;
    private final LocalDateTime imageSubmittedDate;
    private final int currentScore;
    private final int reportCount;
    private final int userTotalReports;
    private final long reportId;


    public ReportEntity(String url, long whoGotReported, long artistReported, long imageReported, LocalDateTime firstReport, String artistName, LocalDateTime imageSubmittedDate, int currentScore, int reportCount, int userTotalReports, long reportId) {
        this.url = url;
        this.whoGotReported = whoGotReported;
        this.artistReported = artistReported;
        this.imageReported = imageReported;
        this.firstReport = firstReport;
        this.artistName = artistName;
        this.imageSubmittedDate = imageSubmittedDate;
        this.currentScore = currentScore;
        this.reportCount = reportCount;
        this.userTotalReports = userTotalReports;
        this.reportId = reportId;
    }


    public String getUrl() {
        return url;
    }

    public long getWhoGotReported() {
        return whoGotReported;
    }

    public long getArtistReported() {
        return artistReported;
    }

    public long getImageReported() {
        return imageReported;
    }

    public LocalDateTime getFirstReport() {
        return firstReport;
    }

    public LocalDateTime getImageSubmittedDate() {
        return imageSubmittedDate;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getReportCount() {
        return reportCount;
    }

    public int getUserTotalReports() {
        return userTotalReports;
    }


    public String getArtistName() {
        return artistName;
    }

    public long getReportId() {
        return reportId;
    }
}
