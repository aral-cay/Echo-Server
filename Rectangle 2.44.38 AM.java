import java.awt.*;

/**
 * A rectangle-shaped Shape
 * Defined by an upper-left corner (x1,y1) and a lower-right corner (x2,y2)
 * with x1<=x2 and y1<=y2
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author CBK, updated Fall 2016
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2024
 * @co-author aral cay
 */
public class Rectangle implements Shape {
	private String id; // identifies the shapes
	private int x1, y1, x2, y2;		// x and y values
	private Color color; //color

	//rectangle is constructed
	public Rectangle(int x1, int y1, Color color) {
		//x, y and color values are matched
		this.x1 = x1; this.x2 = x1;
		this.y1 = y1; this.y2 = y1;

		this.color = color;
	}

	///rectangle is constructed with two corners
	public Rectangle(int x1, int y1, int x2, int y2, Color color) {
		this.color = color;
		setCorners(x1, y1, x2, y2);

	}

	//for rectangles defined by two corners and an id
	public Rectangle(int x1, int y1, int x2, int y2, Color color, String id){
		this.id = id;
		this.color = color;
		setCorners(x1, y1, x2, y2);

	}

	//redefine the rectangle
	public void setCorners(int x1, int y1, int x2, int y2) {
		// max and min bounds are defined

		//for y vals
		this.y1 = Math.min(y1, y2);
		this.y2 = Math.max(y1, y2);

		//for x vals
		this.x1 = Math.min(x1, x2);
		this.x2 = Math.max(x1, x2);

	}

	// TODO: YOUR CODE HERE
	@Override
	//moves it by dx and dy
	public void moveBy(int dx, int dy) {
		x1 += dx;
		x2 += dx;
		y1 += dy;
		y2 += dy;
	}

	@Override
	public Color getColor() {
		//returns the color
		return this.color;
	}

	@Override
	public void setColor(Color color) {
		//sets the color
		this.color = color;
	}
		
	@Override
	public boolean contains(int x, int y) {
		//returns whether the point is contained
		boolean b = ((x > x1) && (x < x2)) && ((y > y1 && y < y2));
		return b;
	}

	@Override
	public void draw(Graphics graphs) {
		//sets color
		graphs.setColor(this.color);
		//fills the rectangle with it
		graphs.fillRect(x1, y1, x2-x1, y2-y1);
	}

	//toString method of the rectangle class
	@Override
	public String toString() {
		//if the id is null it is not included in the return val
		if (this.id == null){
			String s = "rectangle " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + color.getRGB();
			return s;
		}
		//if it is defined, id is included
		else {
			String s = "rectangle "+x1+" "+y1+" "+x2+" "+y2+" "+color.getRGB() + " " + this.id;
			return s;
		}
	}

	@Override
	public String getID() {
		//returns the id
		return this.id;
	}

	@Override
	public void setID(String id){
		//sets the id
		this.id = id;
	}
}
