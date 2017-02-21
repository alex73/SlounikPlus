package org.im.dc.server.db;

import java.util.Date;

import org.im.dc.service.dto.Related;

public class RecArticleHistory {
    private int historyId;
    // артыкул
    private int articleId;
    // словы з артыкула
    private String[] words;
    // калі быў зменены
    private Date changed;
    // хто змяніў
    private String changer;
    // стары стан
    private String oldState;
    // новы стан
    private String newState;
    // старыя загалоўныя словы
    private String[] oldWords;
    // новыя загалоўныя словы
    private String[] newWords;
    // стары спіс карыстальнікаў
    private String[] oldAssignedUsers;
    // новы спіс карыстальнікаў
    private String[] newAssignedUsers;
    // стары XML
    private byte[] oldXml;
    // новы XML
    private byte[] newXml;

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

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

    public Date getChanged() {
        return changed;
    }

    public void setChanged(Date changed) {
        this.changed = changed;
    }

    public String getChanger() {
        return changer;
    }

    public void setChanger(String changer) {
        this.changer = changer;
    }

    public String getOldState() {
        return oldState;
    }

    public void setOldState(String oldState) {
        this.oldState = oldState;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public String[] getOldWords() {
        return oldWords;
    }

    public void setOldWords(String[] oldWords) {
        this.oldWords = oldWords;
    }

    public String[] getNewWords() {
        return newWords;
    }

    public void setNewWords(String[] newWords) {
        this.newWords = newWords;
    }

    public String[] getOldAssignedUsers() {
        return oldAssignedUsers;
    }

    public void setOldAssignedUsers(String[] oldAssignedUsers) {
        this.oldAssignedUsers = oldAssignedUsers;
    }

    public String[] getNewAssignedUsers() {
        return newAssignedUsers;
    }

    public void setNewAssignedUsers(String[] newAssignedUsers) {
        this.newAssignedUsers = newAssignedUsers;
    }

    public byte[] getOldXml() {
        return oldXml;
    }

    public void setOldXml(byte[] oldXml) {
        this.oldXml = oldXml;
    }

    public byte[] getNewXml() {
        return newXml;
    }

    public void setNewXml(byte[] newXml) {
        this.newXml = newXml;
    }

    public Related getRelated() {
        Related r = new Related();
        r.type = Related.RelatedType.HISTORY;
        r.articleId = articleId;
        r.words = words;
        r.id = historyId;
        r.articleId = articleId;
        r.when = changed;
        r.who = changer;
        if (oldState != null && newState != null) {
            r.what = oldState + " -> " + newState;
        } else if (newXml != null) {
            r.what = "Тэкст артыкула";
        }
        return r;
    }
}
