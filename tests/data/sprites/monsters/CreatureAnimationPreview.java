package data.sprites.monsters;


import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class CreatureAnimationPreview {
private static final int NUMBER_OF_ROWS = 4;
private static final int NUMBER_OF_FRAMES = 3;
/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.add(getImageViewerSwing(), null);
			jPanel.add(getImageViewerSwing2(), null);
			jPanel.add(getImageViewerSwing3(), null);
			jPanel.add(getImageViewerSwing4(), null);
			jPanel.add(getImageViewerSwing1(), null);
		}
		return jPanel;
	}
/**
 * This method initializes imageViewerSwing	
 * 	
 * @return data.sprites.monsters.ImageViewerSwing	
 */
private ImageViewerSwing getImageViewerSwing() {
	if (imageViewerSwing == null) {
		imageViewerSwing = new ImageViewerSwing();
		imageViewerSwing.setName("imageViewerSwing");
		imageViewerSwing.setBounds(new Rectangle(20, 20, 96, 168));
	}
	return imageViewerSwing;
}
/**
 * This method initializes imageViewerSwing1	
 * 	
 * @return data.sprites.monsters.ImageViewerSwing	
 */
private ImageViewerSwing getImageViewerSwing1() {
	if (imageViewerSwing1 == null) {
		imageViewerSwing1 = new ImageViewerSwing();
		imageViewerSwing1.setName("imageViewerSwing1");
		imageViewerSwing1.setBounds(new Rectangle(137, 212, 241, 353));
	}
	return imageViewerSwing1;
}
/**
 * This method initializes imageViewerSwing2	
 * 	
 * @return data.sprites.monsters.ImageViewerSwing	
 */
private ImageViewerSwing getImageViewerSwing2() {
	if (imageViewerSwing2 == null) {
		imageViewerSwing2 = new ImageViewerSwing();
		imageViewerSwing2.setName("imageViewerSwing2");
		imageViewerSwing2.setBounds(new Rectangle(130, 20, 96, 168));
	}
	return imageViewerSwing2;
}
/**
 * This method initializes imageViewerSwing3	
 * 	
 * @return data.sprites.monsters.ImageViewerSwing	
 */
private ImageViewerSwing getImageViewerSwing3() {
	if (imageViewerSwing3 == null) {
		imageViewerSwing3 = new ImageViewerSwing();
		imageViewerSwing3.setName("imageViewerSwing3");
		imageViewerSwing3.setBounds(new Rectangle(250,20,96,168));
	}
	return imageViewerSwing3;
}
/**
 * This method initializes imageViewerSwing4	
 * 	
 * @return data.sprites.monsters.ImageViewerSwing	
 */
private ImageViewerSwing getImageViewerSwing4() {
	if (imageViewerSwing4 == null) {
		imageViewerSwing4 = new ImageViewerSwing();
		imageViewerSwing4.setName("imageViewerSwing4");
		imageViewerSwing4.setBounds(new Rectangle(370,20,96,168));
	}
	return imageViewerSwing4;
}

public static void main(String[] args) {
	(new CreatureAnimationPreview()).getJFrame().setVisible(true);
}
	private JFrame jFrame = null;  //  @jve:decl-index=0:visual-constraint="141,108"
	private JSplitPane jSplitPane = null;
	private JScrollPane jScrollPane = null;
	private FileTree jTree = null;
	private JPanel jPanel = null;
	private ImageViewerSwing imageViewerSwing = null;
	private ImageViewerSwing imageViewerSwing1 = null;
	private ImageViewerSwing imageViewerSwing2 = null;
	private ImageViewerSwing imageViewerSwing3 = null;
	private ImageViewerSwing imageViewerSwing4 = null;
	private AnimationRunner[] animations=null;
	
	/**
	 * This method initializes jFrame	
	 * 	
	 * @return javax.swing.JFrame	
	 */
	private JFrame getJFrame() {
		if (jFrame == null) {
			jFrame = new JFrame();
			
			jFrame.setSize(new Dimension(818, 470));
			
			jFrame.setContentPane(getJSplitPane());
			jFrame.setTitle("animated Monsters test");
			jFrame.addWindowListener(new java.awt.event.WindowAdapter() {   
			
				@Override
				public void windowClosing(java.awt.event.WindowEvent e) {
					if (animations!=null) {
						for (int i =0 ; i < animations.length; i++)
						{
							if (animations[i]!=null){
						        animations[i].stopAnimation();
						        animations[i].tearDown();
							}
						}
					}
					 
					 jSplitPane = null;
					 jScrollPane = null;
					 jTree = null;
					 jPanel = null;
					 imageViewerSwing = null;
					 imageViewerSwing1 = null;
					 imageViewerSwing2 = null;
					 imageViewerSwing3 = null;
					 imageViewerSwing4 = null;
					System.out.println("windowClosing()"); 
					super.windowClosing(e);
					System.exit(0);
				}
			});
		}
		return jFrame;
	}
	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setLeftComponent(getJScrollPane());
		//	jSplitPane.setRightComponent(getImageViewer());
			jSplitPane.setRightComponent(getJPanel());
		}
		return jSplitPane;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTree());
		}
		return jScrollPane;
	}
	/**
	 * This method initializes jTree	
	 * 	
	 * @return data.sprites.monsters.FileTree	
	 */
	private FileTree getJTree() {
		if (jTree == null) {
			try {
				File file=null;
				JFileChooser fc=new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(jScrollPane);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		             file = fc.getSelectedFile();
		            jTree = new FileTree(file.getPath());   
		        } else {
		            System.exit(0);
		        }
				
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (SecurityException e1) {
				e1.printStackTrace();
			}
			jTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
				public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
					jFrame.setTitle(e.getNewLeadSelectionPath().getLastPathComponent().toString());
						
							BufferedImage buf=null;
							try {
								File file = new File(  e.getNewLeadSelectionPath().getLastPathComponent().toString());
								  if (file.isDirectory()) {
									return;
								}
								  
								buf = ImageIO.read( file ) ;
						     if (buf==null) {
								return;
							}
							} catch (IOException e1) {
								//should never happen;
								e1.printStackTrace();
								return;
							};
							
							
                               getImageViewerSwing1().setImage(buf);
                           if (animations==null){
                        	   animations= new AnimationRunner[4];
                        	   animations[0] = AnimationCreate(buf,0, getImageViewerSwing());
                               animations[1] = AnimationCreate(buf,1, getImageViewerSwing2());
                        	   animations[2] = AnimationCreate(buf,2, getImageViewerSwing3());
                        	   animations[3] = AnimationCreate(buf,3, getImageViewerSwing4());
                               
                           }else{
                               animations[0].stopAnimation();
                               animations[1].stopAnimation();
                               animations[2].stopAnimation();
                               animations[3].stopAnimation();
                           }
							
                               animations[0].startAnimation(buffersCreate(buf, 0));
                               animations[1].startAnimation(buffersCreate(buf, 1));
                               animations[2].startAnimation(buffersCreate(buf, 2));
                               animations[3].startAnimation(buffersCreate(buf, 3));
				
				}
			});
		}
		return jTree;
	}
	
	AnimationRunner AnimationCreate(BufferedImage buf, int row, ImageViewerSwing imageViewer){
		return new AnimationRunner(imageViewer);
	}
	private BufferedImage[] buffersCreate(BufferedImage buf, int row) {
		BufferedImage[] buffers = new BufferedImage[NUMBER_OF_FRAMES];
        int framewidth=buf.getWidth()/CreatureAnimationPreview.NUMBER_OF_FRAMES;
        
        
        int frameheight = buf.getHeight()/CreatureAnimationPreview.NUMBER_OF_ROWS;
		   for(int i = 0 ; i< NUMBER_OF_FRAMES;i++){
     	   
            buffers[i]=buf.getSubimage(i*framewidth, row*frameheight, framewidth, frameheight);
        
		   }
		return buffers;
	}
	
}
