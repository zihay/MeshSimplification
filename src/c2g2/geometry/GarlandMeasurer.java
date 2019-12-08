package c2g2.geometry;

import java.util.HashMap;
import org.joml.*;


/* Computes quadrics of vertices and cost of edges */
public class GarlandMeasurer extends Measurer {

    /* Create measurer for the given mesh.
     * Stores quadrics in quadMap using vertex ids as keys.
     */
    public GarlandMeasurer(HalfEdgeMesh mesh) {
        this.mesh = mesh;
        this.quadMap = new HashMap<>();
    }

    // call before simplification
    public void init() {
        quadMap.clear();
        for (Vertex v : mesh.getVertices()) updateQuadric(v);
    }

    /* TODO (part 2):
     *   Compute the collapse cost of an edge (v0, v1) and store
     *   the optimal position in newV.
     *
     *   This is step (3) of algorithm in the Garland and Heckbert paper.
     *   Returns the cost of newV.
     */
    public float collapseCost(HalfEdge he, Vertex newV) {
        // Hint: Our representation of a quadric has the form
        //      m = [Q b
        //           0 d]
        //   with inverse
        //   m^-1 = [Q^-1  -(1/d)(Q^-1)*b
        //             0        1/d      ]
        //   so the quadric is invertible <=> Q is invertible

        // student code goes here
        Vertex v1 = he.getFlipE().getNextV();
        Vertex v2 = he.getNextV();
        Quadric q1 = quadMap.get(v1.getId());
        Quadric q2 = quadMap.get(v2.getId());

        Matrix4f M1 = new Matrix4f();
        Matrix4f M2 = new Matrix4f();

        M1.set3x3(q1.Q);
        M1.setTranslation(q1.b);
        M1.m33(q1.d);
        M2.set3x3(q2.Q);
        M2.setTranslation(q2.b);
        M2.m33(q2.d);

        Matrix4f M = new Matrix4f(M1);
        M.add(M2);

        M.invert();

        Vector4f vbar = new Vector4f(0, 0, 0, 1).mul(M);
        vbar.div(vbar.w);
        newV.setPos(new Vector3f(vbar.x, vbar.y, vbar.z));

        //cost
        Matrix4f Q1 = new Matrix4f(M1);
        Matrix4f Q2 = new Matrix4f(M2);
        Q1.setRow(3, new Vector4f(q1.b, q1.d));
        Q2.setRow(3, new Vector4f(q2.b, q2.d));
        Matrix4f Q = new Matrix4f(Q1);
        Q.add(Q2);

        Vector4f res = new Vector4f();
        vbar.mul(Q, res);
        float cost = res.dot(vbar);

        return cost;
    }

    /* Update quadric of newV from v0's and v1's */
    public void edgeCollapsed(Vertex v0, Vertex v1, Vertex newV) {
        int i1 = v0.getId();
        int i2 = v1.getId();
        assert (quadMap.containsKey(i1) && quadMap.containsKey(i2));
        Quadric q = new Quadric();
        quadMap.get(i1).add(quadMap.get(i2), q);
        quadMap.remove(i1);
        quadMap.remove(i2);
        quadMap.put(newV.getId(), q);

    }

    /* TODO (part 2):
     *   Compute quadric at vertex v.
     *
     *   Use face normal (see Face.getNormal()) and orgin (newV) to find
     *   (a,b,c,d) to build the fundamental error quadric K for each
     *   triangle around newV.
     *   Note that the bottom row is zero except for the last value.
     */
    private void updateQuadric(Vertex v) {
        // student code goes here

        Matrix4f K = new Matrix4f();
        K.zero();
        HalfEdge h = v.getEdge();
        do {
            Face f = h.getlFace();
            Vector3f n = f.getNormal();
            float dis = -n.dot(v.getPos());
            Vector4f p = new Vector4f(n, dis);

            float a = p.x;
            float b = p.y;
            float c = p.z;
            float d = p.w;

            Matrix4f Kp = new Matrix4f(a * a, a * b, a * c, a * d,
                    a * b, b * b, b * c, b * d,
                    a * c, b * c, c * c, c * d,
                    a * d, b * d, c * d, d * d);

            K.add(Kp);
            h = h.getFlipE().getNextE();
        } while (h != v.getEdge());

        Matrix3f Q = new Matrix3f();
        Vector3f b = new Vector3f();
        K.get3x3(Q);
        K.getTranslation(b);
        float d = K.m33();

        Quadric q = new Quadric(Q, b, d);
        quadMap.put(v.getId(), q);

    }

}