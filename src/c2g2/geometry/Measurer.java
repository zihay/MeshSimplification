package c2g2.geometry;
import java.util.HashMap;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/* Provides interface to calculate cost */
public abstract class Measurer {

    protected class Quadric {
        Matrix3f Q; // This Q is not the same as the quadric Q in the paper
        Vector3f b; // quadrics are symmetric, so this b is both the right column and bottom row
        float d;    // bottom-right corner

        Quadric() {
            Q = new Matrix3f().zero();
            b = new Vector3f().zero();
        }
        Quadric(Quadric q) {
            Q = new Matrix3f(q.Q);
            b = new Vector3f(q.b);
            d = q.d;
        }

        Quadric(Matrix3f Q, Vector3f b, float d) {
            this.Q = new Matrix3f(Q);
            this.b = new Vector3f(b);
            this.d = d;
        }

        Quadric add(Quadric rhs) {
            Q.add(rhs.Q);
            b.add(rhs.b);
            d += rhs.d;
            return this;
        }

        Quadric add(Quadric rhs, Quadric dest) {
            dest.Q.set(Q);
            dest.b.set(b);
            dest.d = d;
            return dest.add(rhs);
        }
    }

    protected HalfEdgeMesh mesh;

    protected HashMap<Integer, Quadric> quadMap;

    protected final float EP = 1.0e-9f;

    public abstract void init();

    public abstract float collapseCost(HalfEdge he, Vertex newV);

    public abstract void edgeCollapsed(Vertex v0, Vertex v1, Vertex newV);
  
}