package core.util;

import core.apis.last.ConcurrentLastFM;
import dao.ChuuService;

public record Deps(ConcurrentLastFM lastFM, ChuuService db) {
}