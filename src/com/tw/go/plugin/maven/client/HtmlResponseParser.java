package com.tw.go.plugin.maven.client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.maven.nexus.Content;
import com.tw.go.plugin.maven.nexus.ContentItem;

public class HtmlResponseParser {

    private static final int HREF_CELL_INDEX = 0;
    private static final int DATE_CELL_INDEX = 1;
    private static final int SIZE_CELL_INDEX = 2;

    private static final Logger LOGGER = Logger.getLoggerFor(HtmlResponseParser.class);

    public static Content parse(String html, SimpleDateFormat dateFormat) {
        Document doc = Jsoup.parse(html);
        Elements rows = doc.body().getElementsByTag("table").get(0).getElementsByTag("tbody").get(0).getElementsByTag("tr");
        Content result = new Content();
        List<ContentItem> items = new ArrayList<ContentItem>(rows.size());
        LOGGER.debug("Parsing HTML, number of rows to parse: " + rows.size());
        for (Element row: rows) {
            ContentItem item = parseRow2(row, dateFormat);
            if (item != null) items.add(item);
        }
        result.setContentItems(items);
        return result;
    }

    private static ContentItem parseRow2(Element row, SimpleDateFormat sdf) {
        try {
            Elements cells = row.getElementsByTag("td");
            if (cells.size() < 3) return null;
            ContentItem item = new ContentItem();
            boolean isOk = parseHrefCell(cells.get(HREF_CELL_INDEX), item);
            if (isOk) isOk = parseDateCell(cells.get(DATE_CELL_INDEX), item, sdf);
            if (isOk) isOk = parseSizeCell(cells.get(SIZE_CELL_INDEX), item);
            if (isOk) {
                return item;
            } else {
                LOGGER.debug("HTML row: " + row + "is not added to reult.");
                return null;
            }
        } catch (Exception ex) {
            LOGGER.warn("Exception while parsing HTML row: " + row + ". Row is skipped and not added to result.", ex);
            return null;
        }
    }

    private static boolean parseHrefCell(Element cell, ContentItem item) {
        Element href = cell.getElementsByTag("a").get(0);
        String absolutePath = href.attributes().get("href");
        if (!absolutePath.startsWith("http")) return false;
        String versionText = href.text();
        if (versionText.endsWith("/")) {
            item.setLeaf("false");
            versionText = StringUtils.removeEnd(versionText, "/");
        } else {
            item.setLeaf("true");
        }
        item.setResourceURI(absolutePath);
        item.setText(versionText);
        return true;
    }

    private static boolean parseDateCell(Element cell, ContentItem item, SimpleDateFormat sdf) {
        String date = cell.text();
        Date d;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse date [" + date +"] from table cell using package pattern",e);
        }    	
        item.setLastModified(ContentItem.MAVEN_DATE_FORMAT.format(d));
        return true;
    }

    private static boolean parseSizeCell(Element cell, ContentItem item) {
        int sizeOnDisk = -1;
        if (Boolean.valueOf(item.getLeaf())) {
            try {
                sizeOnDisk = Integer.parseInt(cell.text());
            } catch (NumberFormatException n) {
                //nothing needs to happen here.
                //If size is invalid, or there is no size, then directroy is assumed and -1 is used.
            }
        }
        item.setSizeOnDisk(String.valueOf(sizeOnDisk));
        return true;
    }

}
