import java.awt.*;
import java.util.ArrayList;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2024
 * @co-author aral cay
 */
public class Polyline implements Shape {
	// TODO: YOUR CODE HERE

	private String id;
	private Color color; // color
	private int x1,y1,x2,y2; //x and y for the drawing
	ArrayList<Point>points= new ArrayList<Point>(); //points arraylist

	//constructs polyline
	public Polyline(Point p, Color color) {
		//adds point p and colors
		points.add(p);
		this.color = color;
	}


	//gets the id
	@Override
	public String getID() {

		return this.id;
	}

	//sets the id
	@Override
	public void setID(String id) {
			this.id = id;
	}

	// moves the polyline by dx and dy
	@Override
	public void moveBy(int dx, int dy) {
		//for i in points.size
		for(int i = 0;i<points.size();i++) {
			//points are moved
			Point temp = points.get(i);
			points.remove(i);
			points.add(i, new Point(temp.x+dx,temp.y+dy));
		
		}
	}

	//returns color
	@Override
	public Color getColor() {
		return color;
	}

	//sets color
	@Override
	public void setColor(Color color) {
		this.color=color;
	}

	//returns if the point is contained
	@Override
	public boolean contains(int x, int y) {

		for(int i1 = 0; i1<points.size()-1;i1++) {
			//if the point is contained in the shape returns true
			if (Segment.pointToSegmentDistance(x, y, points.get(i1).x, points.get(i1).y, points.get(i1+1).x, points.get(i1+1).y) <= 10) {
				return true;
			}
		}
		//else returns false
		return false;
		
	}

	//draw method
	@Override
	public void draw(Graphics g) {
		//gets the color
		g.setColor(color);
		//for each i as i is smaller than the size of points
		for(int i =0;i<points.size()-1;i++) {
			//draw line
			g.drawLine(points.get(i).x, points.get(i).y, points.get(i+1).x,points.get(i+1).y);
		}
	}

	//tostring method of polyline
	@Override
	public String toString() {

		String s = "";
			for(Point p:points) {
				s += p.x+","+p.y+",";
			}
			return "polyline"+" "+s+" "+color.getRGB();
		
		
	}
	//sets the end, adds p
	public void setEnd(Point p) {
		this.x2=p.x;
		this.y2=p.y;
		points.add(p);
	}
}
