package org.im.dc.client.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * @author Anton Smaliuk <asmaliuk@gmail.com>
 */
public class XMLDiffPanel extends JPanel {
	
	private String firstDoc;
	private String secondDoc;
	private Color coincidenceColor = null;//Color.green;
	private Color diffColor = Color.pink;
	
	private int fontSize = 14;
	
	private int padding = 3;
	private int margin = 1;

	public String getFirstDoc() {
		return firstDoc;
	}

	public void setFirstDoc(String firstDoc) {
		this.firstDoc = firstDoc;
	}

	public String getSecondDoc() {
		return secondDoc;
	}

	public void setSecondDoc(String secondDoc) {
		this.secondDoc = secondDoc;
	}

	public Color getCoincidenceColor() {
		return coincidenceColor;
	}

	public void setCoincidenceColor(Color coincidenceColor) {
		this.coincidenceColor = coincidenceColor;
	}

	public Color getDiffColor() {
		return diffColor;
	}

	public void setDiffColor(Color diffColor) {
		this.diffColor = diffColor;
	}


	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	
	public int getPadding() {
		return padding;
	}

	public void setPadding(int padding) {
		this.padding = padding;
	}

	public int getMargin() {
		return margin;
	}

	public void setMargin(int margin) {
		this.margin = margin;
	}

	public XMLDiffPanel(String first, String second) {
		if(first != null){
			this.firstDoc = first;
		}else{
			this.firstDoc = "";
		}
		
		if(second != null){
			this.secondDoc = second;
		}else{
			this.secondDoc = ""; 
		}
		
		fillPanel();
		
		//System.out.println(common);

	}

	public void fillPanel() {

		List<String> firstLines = formatXml(this.firstDoc);
		List<String> secondLines = formatXml(this.secondDoc);
		List<String> common = searchLongestCommon(firstLines, secondLines);
		
		this.removeAll();
		
		int i =0;
		int j = 0;
		int k = 0;
		int y = -1;
		
		/*if(common.isEmpty()){
			common.add(null);
		}*/
		
		GridBagLayout layout = new GridBagLayout();		
		setLayout(layout);
		setOpaque(false);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 0.5;
		constraints.insets = new Insets(margin, margin, margin, margin);
		
		while(k<common.size() && i < firstLines.size() && j < secondLines.size()){
			y++;
			String commonStr = common.get(k);
			String firstString = firstLines.get(i);
			String secondString = secondLines.get(j);
			
			if(commonStr != null && firstString.trim().equals(commonStr.trim()) && secondString.trim().equals(commonStr.trim())){
				
				constraints.gridx = 0;
				constraints.gridy = y;
				JTextArea labelFirst = new JTextArea(firstString);
				labelFirst.setFont(new Font("Monospaced", Font.PLAIN, 14));
				setLabelProps(labelFirst,coincidenceColor);
				add(labelFirst, constraints);

				constraints.gridx = 1;
				constraints.gridy = y;
				JTextArea labelSecond = new JTextArea(secondString);
				labelSecond.setFont(new Font("Monospaced", Font.PLAIN, 14));
				setLabelProps(labelSecond,coincidenceColor);
				add(labelSecond,constraints);
				
				i++;
				j++;
			}else{
				if(commonStr != null && firstString.trim().equals(commonStr.trim())){

					constraints.gridx = 1;
					constraints.gridy = y;
					JTextArea labelSecond = new JTextArea(secondString);
					labelSecond.setFont(new Font("Monospaced", Font.PLAIN, 14));
					setLabelProps(labelSecond, diffColor);
					add(labelSecond,constraints);
					
					j++;
				}else if(commonStr != null && secondString.trim().equals(commonStr.trim())){
					constraints.gridx = 0;
					constraints.gridy = y;
					JTextArea labelFirst = new JTextArea(firstString);
					labelFirst.setFont(new Font("Monospaced", Font.PLAIN, 14));
					setLabelProps(labelFirst, diffColor);

					add(labelFirst, constraints);

					constraints.gridx = 1;
					constraints.gridy = y;
					JLabel labelSecond = new JLabel("");
					add(labelSecond,constraints);
					
					i++;
					
				}else{
					constraints.gridx = 0;
					constraints.gridy = y;
					JTextArea labelFirst = new JTextArea(firstString);

					labelFirst.setFont(new Font("Monospaced", Font.PLAIN, 14));
					setLabelProps(labelFirst, diffColor);

					add(labelFirst, constraints);

					constraints.gridx = 1;
					constraints.gridy = y;
					JTextArea labelSecond = new JTextArea(secondString);
					labelSecond.setFont(new Font("Monospaced", Font.PLAIN, 14));
					setLabelProps(labelSecond, diffColor);
					add(labelSecond,constraints);
					i++;
					j++;
				}
				continue;
			}
			k++;
		}
		while(i < firstLines.size() || j < secondLines.size()){
			
			y++;
			
			String firstString = null;
			String secondString = null;

			if(firstLines.size()>i){
				firstString = firstLines.get(i);
				i++;
			}
			if(secondLines.size()>j){
				secondString = secondLines.get(j);
				j++;
			}
			if(firstString != null){
				constraints.gridx = 0;
				constraints.gridy = y;
				JTextArea labelFirst = new JTextArea(firstString);
	
				labelFirst.setFont(new Font("Monospaced", Font.PLAIN, 14));
				setLabelProps(labelFirst, diffColor);
	
				add(labelFirst, constraints);
			}

			if(secondString != null){
				constraints.gridx = 1;
				constraints.gridy = y;
				JTextArea labelSecond = new JTextArea(secondString);
				labelSecond.setFont(new Font("Monospaced", Font.PLAIN, 14));
				setLabelProps(labelSecond, diffColor);
				add(labelSecond,constraints);
			}

			
		}
	}

	public void setLabelProps(JTextArea labelFirst,Color bgColor) {
		labelFirst.setFont(new Font("Monospaced", Font.PLAIN, 14));
		labelFirst.setLineWrap(true);
		labelFirst.setEditable(false);
		labelFirst.setFocusable(false);
		if(bgColor==null){
			labelFirst.setOpaque(false);
		}else{
			labelFirst.setOpaque(true);
			labelFirst.setBackground(bgColor);
		}
		labelFirst.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
	}
	
	private List<String> formatXml(String src){

		List<String> lines = new ArrayList<String>();

		String text = src.replaceAll("(?<=>[\t ]{0,128})<", "\n<");
		System.out.println(text);
		String ar[];
		ar = text.split("\n");
		
		for(int i=0;i<ar.length;i++){
			lines.add(ar[i]);
		}
		
		return lines;
	}
	private List<String> searchLongestCommon(List<String> firstLines,List<String> secondLines){
		List<String> lines = new ArrayList<String>();
		int i,j;
		int[][] m = new int[firstLines.size()+1][secondLines.size()+1];
		for(i = firstLines.size();i>= 0;i--){
			for(j = secondLines.size();j>= 0;j--){
				if(i == firstLines.size() || j == secondLines.size()){
					m[i][j]=0;
				}else if(firstLines.get(i).trim().equals(secondLines.get(j).trim())){
					m[i][j] = 1 + m[i+1][j+1];
				}else{
					m[i][j] = Math.max(m[i+1][j], m[i][j+1]); 
				}
			}
			
		}

		i = 0;
		j = 0;
		while(i < firstLines.size() && j < secondLines.size()){
			if(firstLines.get(i).trim().equals(secondLines.get(j).trim())){
				lines.add(firstLines.get(i));
				i++;
				j++;
			}else if(m[i+1][j]>=m[i][j+1]){
				i++;
			}else{
				j++;
			}
		}

		return lines;
	}

}
