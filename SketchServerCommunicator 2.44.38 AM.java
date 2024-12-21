import java.io.*;
import java.awt.*;
import java.net.Socket;
import java.util.*;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2024
 * @co-author aral cay
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");

			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			// TODO: YOUR CODE HERE
			send(server.getSketch().toString());

			// Keep getting and handling messages from the client
			// TODO: YOUR CODE HERE

			String message;
			while ((message = in.readLine()) != null) {
				System.out.println("message received: "+message);

				// handles the client message
				handleClientMessage(message);
			}

			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * takes a message from a client and handles it
	 */
	synchronized public void handleClientMessage(String msg){
		//if there is no message error
		if (msg == null){
			System.err.println("Error");
		}
		//if movingParts.length is smaller than two invalid message
		String[] movingParts = msg.split(" ");

		if (movingParts.length < 2){
			System.err.println("Invalid msg");
			return;
		}

		//switch cases suggested by Java
		switch (movingParts[0]) {
			case "add" -> handleShapeAddMessage(msg);
			case "move" -> handleShapeMoveMessage(msg);
			case "recolor" -> handleShapeRecolorMessage(msg);
			case "delete" -> handleShapeDeleteMessage(msg);
		}
	}

	/**
	 * interprets the request of the client and handles shape message
	 */
	synchronized public void handleShapeAddMessage(String msg){
		//if length is less than seven returns
		String[] movingParts = msg.split(" ");
		if (movingParts.length < 7){
			return;
		}
		//switch cases suggested by Java
		Shape newShape = switch (movingParts[1]) {
			case "ellipse" -> new Ellipse(Integer.parseInt(movingParts[2]), Integer.parseInt(movingParts[3]),
					Integer.parseInt(movingParts[4]), Integer.parseInt(movingParts[5]), new Color(Integer.parseInt(movingParts[6])));
			case "rectangle" -> new Rectangle(Integer.parseInt(movingParts[2]), Integer.parseInt(movingParts[3]),
					Integer.parseInt(movingParts[4]), Integer.parseInt(movingParts[5]), new Color(Integer.parseInt(movingParts[6])));
			case "segment" -> new Segment(Integer.parseInt(movingParts[2]), Integer.parseInt(movingParts[3]),
					Integer.parseInt(movingParts[4]), Integer.parseInt(movingParts[5]), new Color(Integer.parseInt(movingParts[6])));
			default -> null;
		};

		//if new shape is not null
		if (newShape != null){
			//new shape id is changed to
			setNewShapeID(newShape);

			//add the new shape and write it on console
			server.getSketch().addShape(newShape);

			server.broadcast("add " + newShape.toString());
		}
	}

	/**
	 * Handles the shape move function
	 */
	synchronized public void handleShapeMoveMessage(String msg){

		String[] movingParts = msg.split(" ");

		//if the length of movingParts is less than 4 return
		if (movingParts.length < 4){

			return;

		}

		//for each shape in shape list
		for (Shape shape: server.getSketch().getShapeList()){
			//if the shape id is equal to the movingParts[1]
			if (shape.getID().equals(movingParts[1])){
				//then move the shapes
				shape.moveBy(Integer.parseInt(movingParts[2]), Integer.parseInt(movingParts[3]));
				server.broadcast(msg);
			}
		}
	}

	/**
	 * handles the recolor function of the editor
	 */
	synchronized public void handleShapeRecolorMessage(String msg){
		String[] movingParts = msg.split(" ");
		//if the length of movingParts is less than three return
		if (movingParts.length < 3){

			return;
		}

		//for each shape in shape list
		for (Shape shape: server.getSketch().getShapeList()){
			//if the id of the shape equals movingParts[1]
			if (shape.getID().equals(movingParts[1])){
				//set the color of the shape tp the new one
				shape.setColor(new Color(Integer.parseInt(movingParts[2])));
				server.broadcast(msg);
			}
		}

	}

	/**
	 * this method handle the deleting function of the editor
	 */
	synchronized public void handleShapeDeleteMessage(String msg){
		String[] movingParts = msg.split(" ");
		//if the length of movingParts is less than two return
		if (movingParts.length < 2){
			return;
		}

		//iterator define
		Iterator<Shape> iter = server.getSketch().getShapeList().iterator();

		//while there is another element
		while (iter.hasNext()){
			//shape equals iter.next
			Shape shape = iter.next();
			//when the element is finally found delete it
			if (shape.getID().equals(movingParts[1])){
				iter.remove();
				server.broadcast(msg);
			}
		}
	}

	/**
	 * sets the new shape id
	 */
	synchronized public void setNewShapeID(Shape shape){
		//set the shape of the id to id
		String id = UUID.randomUUID().toString();
		shape.setID(id);
	}
}
