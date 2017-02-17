package org.im.dc.server.db;

import java.util.Date;

/**
 * Record from Articles table representation.
 */
public class RecArticle {
    private int articleId;
    // загалоўныя словы
    private String[] words;
    // XML артыкула
    private byte[] xml;
    // імёны прыпісаных карыстальнікаў
    private String[] assignedUsers;
    // стан
    private String state;
    // нататнік
    private String notes;
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

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public String[] getWords() {
        return words;
    }

    public void setWords(String[] words) {
        this.words = words;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
}
