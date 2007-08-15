package data.sprites.monsters;

/**
 *
 */

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class FileTree extends JTree {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public FileTree(String path) throws FileNotFoundException {
		super((TreeModel) null); // Create the JTree itself

		// Use horizontal and vertical lines
		putClientProperty("JTree.lineStyle", "Angled");

		// Create the first node
		FileTreeNode rootNode = new FileTreeNode(null, path);

		// Populate the root node with its subdirectories
		rootNode.populateDirectories(true);
		setModel(new DefaultTreeModel(rootNode));

		// Listen for Tree Selection Events
		addTreeExpansionListener(new TreeExpansionHandler());
	}

	// Returns the full pathname for a path, or null
	// if not a known path
	public String getPathName(TreePath path) {
		Object o = path.getLastPathComponent();
		if (o instanceof FileTreeNode) {
			return ((FileTreeNode) o).file.getAbsolutePath();
		}
		return null;
	}

	// Returns the File for a path, or null if not a known path
	public File getFile(TreePath path) {
		Object o = path.getLastPathComponent();
		if (o instanceof FileTreeNode) {
			return ((FileTreeNode) o).file;
		}
		return null;
	}

	// Inner class that represents a node in this
	// file system tree
	protected class FileTreeNode extends DefaultMutableTreeNode {
		/**
		 *
		 */
		private static final long serialVersionUID = 3223106240309250204L;

		public FileTreeNode(File parent, String name) throws FileNotFoundException {
			this.name = name;

			// See if this node exists and whether it
			// is a directory
			file = new File(parent, name);
			if (!file.exists()) {
				throw new FileNotFoundException("File " + name
						+ " does not exist");
			}

			isDir = file.isDirectory();

			// Hold the File as the user object.
			setUserObject(file);

		}

		// Override isLeaf to check whether this is a directory
		@Override
		public boolean isLeaf() {
			return !isDir;
		}

		// Override getAllowsChildren to check whether
		// this is a directory
		@Override
		public boolean getAllowsChildren() {
			return isDir;
		}

		// For display purposes, we return our own name public String toString()
		// { return name; }
		// If we are a directory, scan our contents and populate
		// with children. In addition, populate those children
		// if the "descend" flag is true. We only descend once,
		// to avoid recursing the whole subtree.
		// Returns true if some nodes were added
		boolean populateDirectories(boolean descend) {
			boolean addedNodes = false;
			// Do this only once
			if (!populated) {
				if (interim) {
					// We have had a quick look here before:
					// remove the dummy node that we added last time
					removeAllChildren();
					interim = false;
				}

				String[] names = file.list(); // Get list of contents

				// Process the directories
				for (int i = 0; i < names.length; i++) {
					String name = names[i];
					try {
						// if (d.isDirectory()) {
						FileTreeNode node = new FileTreeNode(file, name);
						this.add(node);
						if (descend) {
							node.populateDirectories(false);
						}
						addedNodes = true;
						if (!descend) {
							// Only add one node if not descending
							break;
						}
						// }
						// else{

						// }
					} catch (Throwable t) {
						// Ignore phantoms or access problems
					}
				}

				// If we were scanning to get all subdirectories,
				// or if we found no subdirectories, there is no
				// reason to look at this directory again, so
				// set populated to true. Otherwise, we set interim
				// so that we look again in the future if we need to
				if (descend  || !addedNodes) {
					populated = true;
				} else {
					// Just set interim state
					interim = true;
				}
			}
			return addedNodes;
		}

		protected File file; // File object for this node

		protected String name; // Name of this node

		protected boolean populated;

		// true if we have been populated
		protected boolean interim;

		// true if we are in interim state
		protected boolean isDir; // true if this is a directory
	}

	// Inner class that handles Tree Expansion Events
	protected class TreeExpansionHandler implements TreeExpansionListener {
		public void treeExpanded(TreeExpansionEvent evt) {
			TreePath path = evt.getPath(); // The expanded path
			JTree tree = (JTree) evt.getSource(); // The tree

			// Get the last component of the path and
			// arrange to have it fully populated.
			FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
			if (node.populateDirectories(true)) {
				((DefaultTreeModel) tree.getModel()).nodeStructureChanged(node);
			}
		}

		public void treeCollapsed(TreeExpansionEvent evt) {
			// Nothing to do
		}
	}
}
