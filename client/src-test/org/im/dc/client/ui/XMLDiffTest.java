package org.im.dc.client.ui;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * @author Anton Smaliuk <asmaliuk@gmail.com>
 */
public class XMLDiffTest {

	public static void main(String[] args) {
		
		JFrame testFrame = new JFrame("Tet XML Comparison");
		XMLDiffPanel xmlPanel = new XMLDiffPanel("<root><slova>АНАНАC</slova>\n\t\t<zah><gram>м-</gram></zah><tlum><desc>Травяністая трапічная расліна</desc></tlum><tlum><desc>Ручная граната</desc></tlum></root>",
				"<root><slova>АНАНАC</slova><zah><gram>м.</gram></zah><tlum><desc>Травяністая трапічная расліна</desc><examp>Прыклад</examp></tlum></root>");
		
		xmlPanel.setCoincidenceColor(Color.green);
		xmlPanel.fillPanel();
		JScrollPane pane = new JScrollPane(xmlPanel);
		testFrame.getContentPane().add(new JScrollPane(pane));

		testFrame.setSize(800, 300);
		testFrame.setVisible(true);

	}

}
