package org.im.dc.server.db;

import java.util.Date;

import org.im.dc.service.dto.Related;

public class RecArticleHistory {
    private int historyId;
    // тып артыкула
    private String articleType;
    // артыкул
    private int articleId;
    // загаловак артыкула
    private String header;
    // калі быў зменены
    private Date changed;
    // хто змяніў
    private String changer;
    // стары стан
    private String oldState;
    // новы стан
    private String newState;
    // стары загаловак
    private String oldHeader;
    // новы загаловак
    private String newHeader;
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

    public String getArticleType() {
        return articleType;
    }

    public void setArticleType(String articleType) {
        this.articleType = articleType;
    }

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
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

    public String getOldHeader() {
        return oldHeader;
    }

    public void setOldHeader(String oldHeader) {
        this.oldHeader = oldHeader;
    }

    public String getNewHeader() {
        return newHeader;
    }

    public void setNewHeader(String newHeader) {
        this.newHeader = newHeader;
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
        r.header = header;
        r.articleTypeId = articleType;
        r.id = historyId;
        r.articleId = articleId;
        r.when = changed;
        r.who = changer;
        if (newState != null) {
            r.what = oldState + " -> " + newState;
            r.sk = "Ст";
        } else if (newXml != null) {
            r.what = "Тэкст";
            r.sk = "Са";
        } else if (newHeader != null) {
            r.what = "Загаловак";
            r.sk = "Сг";
        }
        return r;
    }
}
