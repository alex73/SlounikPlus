/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.im.dc.client.ui;

/**
 *
 * @author alex
 */
public class ArticlePanelNotes extends javax.swing.JPanel {

    /**
     * Creates new form ArticlePanelNotes
     */
    public ArticlePanelNotes() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        txtNotes = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout());

        txtNotes.setColumns(20);
        txtNotes.setRows(5);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/im/dc/client/ui/Bundle"); // NOI18N
        txtNotes.setToolTipText(bundle.getString("ArticleEditDialog.txtNotes.toolTipText")); // NOI18N
        jScrollPane2.setViewportView(txtNotes);

        add(jScrollPane2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JScrollPane jScrollPane2;
    public javax.swing.JTextArea txtNotes;
    // End of variables declaration//GEN-END:variables
}