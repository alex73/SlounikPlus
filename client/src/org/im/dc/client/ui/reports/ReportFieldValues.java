/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.im.dc.client.ui.reports;

/**
 *
 * @author alex
 */
public class ReportFieldValues extends javax.swing.JPanel {

    /**
     * Creates new form FieldsReportInput
     */
    public ReportFieldValues() {
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        cbField = new javax.swing.JComboBox<>();
        btnSaveAs = new javax.swing.JButton();

        add(cbField);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/im/dc/client/ui/Bundle"); // NOI18N
        btnSaveAs.setText(bundle.getString("ReportFieldValues.btnSaveAs")); // NOI18N
        add(btnSaveAs);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton btnSaveAs;
    public javax.swing.ButtonGroup buttonGroup1;
    public javax.swing.JComboBox<String> cbField;
    // End of variables declaration//GEN-END:variables
}