package dao.entities;

import java.time.LocalDateTime;

public record ReportEntity(String url, long whoGotReported, long artistReported, long imageReported,
                           LocalDateTime firstReport, String artistName, LocalDateTime imageSubmittedDate,
                           int currentScore, int reportCount, int userTotalReports, long reportId) {
}
