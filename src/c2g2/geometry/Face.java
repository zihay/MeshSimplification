package c2g2.geometry;

import org.joml.Vector3f;

public class Face {
	private HalfEdge e;
	
	//// Add more data structure or code here if needed
    public Face() {}

    public Face(HalfEdge e0) {
        this.e = e0;
    }

    public void setEdge(HalfEdge e0) {
        this.e = e0;
    }

    public HalfEdge getEdge() {
        return e;
    }

    // Return non-unit normal
    public Vector3f getNormal() {
        Vertex v0 = e.getNextV();
        Vertex v1 = e.getNextE().getNextV();
        Vertex v2 = e.getNextE().getNextE().getNextV();

        Vector3f p10 = new Vector3f(v1.getPos()).sub(v0.getPos());
        Vector3f p20 = new Vector3f(v2.getPos()).sub(v0.getPos());
        p10.cross(p20).normalize();

        return p10;
    }
}
