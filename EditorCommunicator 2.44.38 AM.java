import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Iterator;

/**
 * Handles communication to/from the server for the editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2024
 * @co-author aral cay
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;		// to server
	private BufferedReader in;		// from server
	protected Editor editor;		// handling communication for

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");

		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		}

		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {
			// handle messages
			// TODO: YOUR CODE HERE
			String message;  //creates the string message
			while ((message = in.readLine()) != null) {
				System.out.println("got msg "+message);
				handleMsg(message); //handles the message
				editor.repaint();  //repaints
			}
		}
		catch (IOException e) {
			e.printStackTrace();  // if not found, error is caught here
		}
		finally {
			System.out.println("server executed");  //finally message is ran at the end
		}
	}

	public void handleMsg(String msg){  //handles the message
		if (msg == null){
			return;  // if null, returns
		}

		String[] parts = msg.split(" ");  // parts splits the string
		if (parts.length < 2){  // if len<2 invalid length
			System.err.println("Invalid message from server");
		}

		if (parts[0].equals("add")){  // all cases are laid out, valid cases are: add, recolor, move, delete, sketch
			handleAdd(msg);
		}
		else if (parts[0].equals("move")){
			handleMove(msg);
		}
		else if (parts[0].equals("recolor")){
			handleRecolor(msg);
		}

		else if (parts[0].equals("sketch")){
			initializeSketch(msg);
		}
		else if (parts[0].equals("delete")){
			handleDelete(msg);
		}

	}


	synchronized public void handleAdd(String msg){  // handles add
		String[] parts = msg.split(" ");    // splits message at spaces
		if (parts.length < 8){  // if parts length is less than 8, returns
			return;
		}

		//Java suggested switch cases
		Shape newShape = switch (parts[1]) {
			//for ellipse
            case "ellipse" -> new Ellipse(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), new Color(Integer.parseInt(parts[6])), parts[7]);
          	//for rectangle
            case "rectangle" -> new Rectangle(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), new Color(Integer.parseInt(parts[6])), parts[7]);
            //for segment
			case "segment" -> new Segment(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), new Color(Integer.parseInt(parts[6])), parts[7]);
            default -> null;
        };

		//if shape is not null
        if (newShape != null){
			//new shape is added and repainted
			editor.getSketch().addShape(newShape);
			editor.repaint();
		}
	}


	//handle move method
	synchronized public void handleMove(String msg){
		//arraylist parts
		String[] parts = msg.split(" ");
		// if length is smaller than 4 returns
		if (parts.length < 4){
			return;
		}

		//for each shape in shapelist
		for (Shape shape: editor.getSketch().getShapeList()){
			//if the id is equal to parts[1] it is moved
			if (shape.getID().equals(parts[1])){
				shape.moveBy(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
			}
		}
	}

	//color handler method
	synchronized public void handleRecolor(String msg){
		String[] parts = msg.split(" ");
		//if parts length is smaller than 3 returns
		if (parts.length < 3){
			return;
		}

		//for each shape in shape list
		for (Shape shape: editor.getSketch().getShapeList()){
			//if shape id is equal to parts[1]
			if (shape.getID().equals(parts[1])){
				//sets color
				shape.setColor(new Color(Integer.parseInt(parts[2])));
			}
		}

	}

	//method to handle delete
	synchronized public void handleDelete(String msg){
		String[] parts = msg.split(" ");
		//if parts length smaller than 2 return
		if (parts.length < 2){
			return;
		}

		//iterator definition
		Iterator<Shape> shapeIterator = editor.getSketch().getShapeList().iterator();
		//while iterator has next
		while (shapeIterator.hasNext()){
			Shape shape = shapeIterator.next();
			//if shape id is equal to parts[1]
			if (shape.getID().equals(parts[1])){
				//iterate
				shapeIterator.remove();
				editor.delete();
				editor.repaint();
			}
		}

	}

	//this method initalizes the sketch
	public void initializeSketch(String msg){
		String shapeList = msg.substring(msg.indexOf("{")+1, msg.indexOf("}"));
		String[] shapes = shapeList.split(", ");
		System.out.println(shapes[0]);

		//for each shape str in shapes
		for (String shapeStr: shapes){
			String[] parts = shapeStr.split(" ");
			//if parts length is less than 7 continues
			if (parts.length < 7) continue;
			//if it equals ellipse
			if (parts[0].equals("ellipse")){
				//handles it for ellipse
				editor.getSketch().addShape(new Ellipse(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
						Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), new Color(Integer.parseInt(parts[5])), parts[6]));
			}
			//if it is rectangle
			else if (parts[0].equals("rectangle")){
				//handles it for rectangle
				editor.getSketch().addShape(new Rectangle(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
					Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), new Color(Integer.parseInt(parts[5])), parts[6]));
			}
			//if it is a segment
			else if (parts[0].equals("segment")){
				//handles it for segment
				editor.getSketch().addShape(new Segment(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
						Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), new Color(Integer.parseInt(parts[5])), parts[6]));
			}
		}
	}
	
}
