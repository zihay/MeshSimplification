package c2g2.geometry;

import org.joml.Vector3f;

/* 
 * Vertex referred by the HalfEdge data structure
 * 
 * Please add code if needed
 */
public class Vertex {
	
    private int id; // index in vertex array
	private HalfEdge e = null;
	private Vector3f pos = null;
	private Vector3f norm = null;

    /*
     * Creates a Vertex object with position (x,y,z) and normal (nx, ny, nz).
     * Each vertex in a mesh must have a unique id for tracking purposes. This
     * will be useful in part 2 as vertices are removed from mesh's array.
     */
	public Vertex(int i, float x, float y, float z, float nx, float ny, float nz) {
        id = i;
        pos = new Vector3f(x, y, z);
        norm = new Vector3f(nx, ny, nz);
	}

    public Vertex(int i, Vector3f pos, Vector3f norm) {
        this.id = i;
        this.pos = new Vector3f(pos);
        this.norm = new Vector3f(norm);
    }

    public Vertex(Vertex v) {
        this.id = v.id;
        this.pos = new Vector3f(v.pos);
        this.norm = new Vector3f(v.norm);
    }
	
	public void setEdge(HalfEdge e0){
		e = e0;
	}
	public boolean hasEdge(){
		return e!=null;
	}
    public void setPos(Vector3f pos) {
        this.pos = pos;
    }
    public void setNorm(Vector3f norm) {
        this.norm = norm;
    }
	public HalfEdge getEdge(){
		return e;
	}
    public Vector3f getPos(){
        return pos;
    }
    public Vector3f getNorm(){
        return norm;
    }
    public void setId(int i) {
        id = i;
    }
    public int getId() {
        return id;
    }

    public Vertex getAverage(Vertex other) {
        if ((pos == null) || (norm == null) || 
            (other.getPos() == null) || (other.getNorm() == null))
            return null;
        float x = pos.x() + other.getPos().x(), 
            y = pos.y() + other.getPos().y(), 
            z = pos.z() + other.getPos().z();
        float nx = norm.x() + other.getNorm().x(), 
            ny = norm.y() + other.getNorm().y(), 
            nz = norm.z() + other.getNorm().z();

        Vertex newV = new Vertex(id, x/2, y/2, z/2, nx/2, ny/2, nz/2);
        return newV;
    }

    public String toString() {
        return id + ": " +pos.toString();
    }
}
