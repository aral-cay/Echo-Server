import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Client-server graphical editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2024
 * @co-author aral cay
 */

public class Editor extends JFrame {
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	public void delete(){
		this.curr = null;
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};

		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});

		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "rectangle", "segment", "freehand"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE
		for (Shape shape: sketch.getShapeList()){  //for each shape in the shape list
			shape.draw(g);  //draw the shape, graphics g
		}
		if (curr != null){  //if the current is not null
			curr.draw(g); //draw g(graphics)
		}
	}

	// Helpers for event handlers

	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		// TODO: YOUR CODE HERE
		if (mode == Editor.Mode.DRAW){ // if in the draw mode
			drawFrom = p; //in draw form p
			moveFrom = p;
            switch (shapeType) {  // java suggested method instead of using if statements
                case "ellipse" -> curr = new Ellipse(drawFrom.x, drawFrom.y, color); //ellipse case
                case "rectangle" -> curr = new Rectangle(drawFrom.x, drawFrom.y, color);  //rectangle case
                case "freehand" -> curr = new Polyline(drawFrom, color);  //freehand polyline case
                case "segment" -> curr = new Segment(drawFrom.x, drawFrom.y, color);  //segment case
            }
		}
		else if (mode == Editor.Mode.MOVE){  // if in the move mode
			for (Shape shape: this.sketch.getShapeList()){  // for each shape
				if (shape.contains(p.x, p.y)){  //if contains point
					this.curr = shape;  // select as current shape
					moveFrom = p;  //move
					handleDrag(p);  //handle drag
				}
			}
		}
		else if (mode == Editor.Mode.RECOLOR){  // if in the recolor mode
			for (Shape shape: this.sketch.getShapeList()){  //for each shape in the shape list
				if (shape.contains(p.x, p.y)){
					if (shape.getID() != null){  // get id is not null
						comm.send("recolor " + shape.getID() + " " + this.color.getRGB());  //recolor
					}
				}
			}
		}
		else if (mode ==  Editor.Mode.DELETE){  // if in the delete mode
			for (Shape shape: this.sketch.getShapeList()){  // for each shape in the list
				if (shape.getID() != null){
					comm.send("delete " + shape.getID());  // delete and communicate message
				}
			}
		}
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {  // handles drag for the point p
		// TODO: YOUR CODE HERE
		if (mode == Editor.Mode.DRAW){  // if in the drawing mode
			if (curr != null){  // if current is not null
                switch (shapeType) {  // switch type recommended by java
                    case "ellipse" -> {  //ellipse case
                        ((Ellipse) curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);  //moves based on the center of the object
                        moveFrom = new Point(drawFrom.x + (p.x - drawFrom.x) / 2, drawFrom.y + (p.y - drawFrom.y) / 2);
                    }
                    case "freehand" -> {  //poly case
                        ((Polyline) curr).setEnd(p);
                        moveFrom = new Point(drawFrom.x + (p.x - drawFrom.x) / 2, drawFrom.y + (p.y - drawFrom.y) / 2);
                    }
                    case "rectangle" -> {  //rectangle case
                        ((Rectangle) curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
                        // The shape should move based on where its center is rather than its top left corner
                        moveFrom = new Point(drawFrom.x + (p.x - drawFrom.x) / 2, drawFrom.y + (p.y - drawFrom.y) / 2);
                    }
                    case "segment" -> {  //segmenet case
                        ((Segment) curr).setEnd(p.x, p.y);
                        moveFrom = new Point((p.x - drawFrom.x) / 2, (p.y - drawFrom.y) / 2);
                    }
                }
				repaint();  //repaints at the end
			}
		}
		else if (mode == Editor.Mode.MOVE){  //move mode
			if (curr != null && moveFrom != null && curr.contains(p.x, p.y)){  //if not empty and contains point p
				if (curr.getID() != null){  // if id not null
					comm.send("move " + curr.getID() + " " + (p.x - moveFrom.x) + " " + (p.y-moveFrom.y));
				}  //moves and sends message, after that repaints
				moveFrom = p;
				repaint();
			}
		}
	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it
	 */
	private void handleRelease() {  //handles release
		// TODO: YOUR CODE HERE
		if (mode == Mode.DRAW){  // if in the draw mode
			comm.send("add " + curr.toString());  //sends toString
			curr = null;  //current is handled, and null
		}
	}


	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();

			}
		});
	}
}
