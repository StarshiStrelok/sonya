/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ss.sonya.server.listener;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * Filter.
 * @author ss
 */
@WebFilter("/*")
public class StatsFilter implements Filter {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(StatsFilter.class);
    /** Number of hours per day. */
    private static final int HOURS_PER_DAY = 24;
    /** Show statistic for special request. */
    private static final String SHOW_STAT_URL = "/rest/data/statistic";
    /** Statistic storage. */
    private static final AtomicInteger[] STORAGE =
            new AtomicInteger[HOURS_PER_DAY];
    /** Current hour. Used for hour changing check. */
    private static int currentHour;
    /**
     * Init storage inside static block.
     */
    static {
        for (int i = 0; i < HOURS_PER_DAY; i++) {
            STORAGE[i] = new AtomicInteger(0);
        }
        currentHour = LocalDateTime.now().getHour();
        LOG.info("statistic storage created, current hour ["
                + currentHour + "]");
    }
    @Override
    public void init(final FilterConfig fc) throws ServletException {
        LOG.info("statistic filter initialization complete...");
    }
    @Override
    public void doFilter(final ServletRequest sr, final ServletResponse sr1,
            final FilterChain fc) throws IOException, ServletException {
        // I'm use default time zone,
        // but may set it during init phase of filter.
        LocalDateTime time = LocalDateTime.now();  // since java 8
        int nowHour = time.getHour();
        if (nowHour != currentHour) {
            // switch to next hour
            STORAGE[nowHour].set(0);    // reset statistic for previous period
            LOG.info("switch storage from [" + currentHour
                    + "] to [" + nowHour + "] index");
            currentHour = nowHour;
        }
        // +1 request
        STORAGE[nowHour].incrementAndGet();
        HttpServletRequest request = (HttpServletRequest) sr;
        if (SHOW_STAT_URL.equals(request.getRequestURI())) {
            LOG.info("print statistic now");
            HttpServletResponse response = (HttpServletResponse) sr1;
            try (PrintWriter writer = response.getWriter()) {
                writer.print(printStatistic());
            }
        } else {    // skip request
            fc.doFilter(sr, sr1);
        }
    }
    @Override
    public void destroy() {
        LOG.info("statistic filter destroy...");
    }
    /**
     * Print statistic as table.
     * @return statistic table.
     */
    private static String printStatistic() {
        String taskFor = "https://careers.epam.by/java-challenge/"
                + "servlet-filters-java-challenge";
        StringBuilder sb = new StringBuilder(taskFor).append("<br/><br/>")
                .append("<table>");
        sb.append("<thead>");
        sb.append("<th>").append("Hour").append("</th>");
        sb.append("<th>").append("Requests").append("</th>");
        sb.append("</thead>");
        for (int i = 0; i < HOURS_PER_DAY; i++) {
            sb.append("<tr>");
            sb.append("<th>").append(i).append("</th>");
            sb.append("<th>").append(STORAGE[i].get()).append("</th>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }
}
