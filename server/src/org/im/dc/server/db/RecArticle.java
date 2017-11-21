package org.im.dc.server.db;

import java.util.Date;

/**
 * Record from Articles table representation.
 */
public class RecArticle {
    private int articleId;
    private String articleType;
    // загаловак
    private String header;
    // XML артыкула
    private byte[] xml;
    // імёны прыпісаных карыстальнікаў
    private String[] assignedUsers;
    // стан
    private String state;
    // пазнакі
    private String[] markers;
    // хто сочыць
    private String[] watchers;
    // на якія словы спасылаецца
    private String[] linkedTo;
    // усе словы - для пошуку
    private String textForSearch;
    // колькасць знакаў для статыстыкі
    private int lettersCount;
    // апошняе абнаўленне - для optimistic locks
    private Date lastUpdated;
    // памылка валідацыі
    private String validationError;

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public String getArticleType() {
        return articleType;
    }

    public void setArticleType(String articleType) {
        this.articleType = articleType;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public byte[] getXml() {
        return xml;
    }

    public void setXml(byte[] xml) {
        this.xml = xml;
    }

    public String[] getAssignedUsers() {
        return assignedUsers;
    }

    public void setAssignedUsers(String[] assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String[] getMarkers() {
        return markers;
    }

    public void setMarkers(String[] markers) {
        this.markers = markers;
    }

    public String[] getWatchers() {
        return watchers;
    }

    public void setWatchers(String[] watchers) {
        this.watchers = watchers;
    }

    public String[] getLinkedTo() {
        return linkedTo;
    }

    public void setLinkedTo(String[] linkedTo) {
        this.linkedTo = linkedTo;
    }

    public String getTextForSearch() {
        return textForSearch;
    }

    public void setTextForSearch(String textForSearch) {
        this.textForSearch = textForSearch;
    }

    public int getLettersCount() {
        return lettersCount;
    }

    public void setLettersCount(int lettersCount) {
        this.lettersCount = lettersCount;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getValidationError() {
        return validationError;
    }

    public void setValidationError(String validationError) {
        this.validationError = validationError;
    }
}
