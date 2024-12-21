import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @co-author aral cay
 */
public class Sketch {
    // shape list is defined
    private final List<Shape> shapesList;

    //constructor for Sketch
    public Sketch(){
        this.shapesList = new ArrayList<Shape>();
    }

    //delete shape method
    public void deleteShape(Shape delete){
        //if the id is null error
        if (delete.getID() == null){
            System.err.println("Error: no id");
        }

        Iterator<Shape> itr = shapesList.iterator();
        //iterates through shapes and deletes them
        while (itr.hasNext()){
            Shape temporary;
            temporary = itr.next();
            if (temporary.getID().equals(delete.getID())){
                itr.remove();
            }
        }
    }

    //add shape method
    public void addShape(Shape shape){

        this.shapesList.add(shape);
    }

  //return shapes
    public List<Shape> getShapeList(){

        return this.shapesList;
    }

    //to string method
    @Override
    public String toString(){

        String returnVal = "Sketch output: { ";
        for (Shape shape: shapesList){
            returnVal += shape.toString();
            returnVal += ", ";
        }

        returnVal += " }";
        return returnVal;

    }
}
