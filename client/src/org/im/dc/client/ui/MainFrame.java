package org.im.dc.client.ui;

public class MainFrame extends javax.swing.JFrame {

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
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

        jMenuBar1 = new javax.swing.JMenuBar();
        menuCommon = new javax.swing.JMenu();
        miResetDesk = new javax.swing.JMenuItem();
        miSettings = new javax.swing.JMenuItem();
        miValidateFull = new javax.swing.JMenu();
        miValuesFull = new javax.swing.JMenu();
        miReports = new javax.swing.JMenu();
        miExportFull = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/im/dc/client/ui/Bundle"); // NOI18N
        setTitle(bundle.getString("MainFrame.title")); // NOI18N

        menuCommon.setText(bundle.getString("MainFrame.menuCommon.text")); // NOI18N

        miResetDesk.setText(bundle.getString("MainFrame.miResetDesk.text")); // NOI18N
        menuCommon.add(miResetDesk);

        miSettings.setText(bundle.getString("MainFrame.miSettings.text")); // NOI18N
        menuCommon.add(miSettings);

        miValidateFull.setText(bundle.getString("MainFrame.miValidateFull.text")); // NOI18N
        menuCommon.add(miValidateFull);

        miValuesFull.setText(bundle.getString("MainFrame.miValuesFull.text")); // NOI18N
        menuCommon.add(miValuesFull);

        miReports.setText(bundle.getString("MainFrame.miReports.text")); // NOI18N
        menuCommon.add(miReports);

        miExportFull.setText(bundle.getString("MainFrame.miExportFull.text")); // NOI18N
        menuCommon.add(miExportFull);

        jMenuBar1.add(menuCommon);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JMenuBar jMenuBar1;
    public javax.swing.JMenu menuCommon;
    public javax.swing.JMenu miExportFull;
    public javax.swing.JMenu miReports;
    public javax.swing.JMenuItem miResetDesk;
    public javax.swing.JMenuItem miSettings;
    public javax.swing.JMenu miValidateFull;
    public javax.swing.JMenu miValuesFull;
    // End of variables declaration//GEN-END:variables
}
