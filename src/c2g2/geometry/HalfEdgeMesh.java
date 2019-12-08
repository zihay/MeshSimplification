package c2g2.geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;


import c2g2.engine.graph.Mesh;

/*
 * A mesh represented by HalfEdge data structure
 */
public class HalfEdgeMesh {

    private ArrayList<HalfEdge> halfEdges;
    private ArrayList<Vertex> vertices;

    private ArrayList<Face> deletedFace = new ArrayList<>();
    private ArrayList<HalfEdge> deletedEdge = new ArrayList<>();
    /* TODO (part 1):
     *   Build HalfEdge mesh from a triangle mesh.
     *
     *   Before beginning, take a look at the Vertex, Face and HalfEdge
     *   classes to see the fields you will need to set.
     */
    public HalfEdgeMesh(Mesh mesh) {

        float[] pos = mesh.getPos();
        float[] norms = mesh.getNorms();
        int[] inds = mesh.getInds();
        Map<Integer,Map<Integer,HalfEdge>> map = new HashMap<>();

        halfEdges = new ArrayList<>();
        vertices = new ArrayList<>();

        // Hint: You will need multiple passes of the arrays to set up
        // each geometry object. A suggestion is to:
        //   (1) Create vertex list
        //   (2) Create half edge list and set faces
        //   (3) Set flip edges

        // student code starts here
        for (int i = 0; i < pos.length / 3; i++) {
            Vector3f p = new Vector3f(pos[3 * i], pos[3 * i + 1], pos[3 * i + 2]);
            Vector3f n = new Vector3f(norms[3 * i], norms[3 * i + 1], norms[3 * i + 2]);
            Vertex v = new Vertex(i, p, n);
            vertices.add(v);
        }

        for (int i = 0; i < inds.length / 3; i++) {
            Face f = new Face();
            for (int j = 0; j < 3; j++) {
                int idx1 = inds[3 * i + j];
                int idx2 = inds[3 * i + ((j + 1) % 3)];
                Vertex a = vertices.get(idx1);
                Vertex b = vertices.get(idx2);
                HalfEdge h = new HalfEdge();
                f.setEdge(h);
                h.setNextV(b);
                h.setlFace(f);
                halfEdges.add(h);
                a.setEdge(h);
                Map<Integer,HalfEdge> subMap = map.getOrDefault(idx1,new HashMap<>());
                subMap.put(idx2,h);
                map.put(idx1,subMap);
            }
            for (int j = 0; j < 3; j++) {
                HalfEdge h1 = halfEdges.get(3 * i + j);
                HalfEdge h2 = halfEdges.get(3 * i + (j + 1) % 3);
                h1.setNextE(h2);
            }
        }
        int i = 0;
        for (HalfEdge h : halfEdges) {
            Vertex v1 = h.getNextE().getNextE().getNextV();
            Vertex v2 = h.getNextV();
            HalfEdge other = map.get(v2.getId()).get(v1.getId());
            h.setFlipE(other);

            //add id
            h.setId(i);
            i++;
        }

        for (HalfEdge he : halfEdges) {
            assert he.getFlipE() != he.getNextE();
        }

//        Map<Face, Integer> count = new HashMap<>();
//        for (HalfEdge he : halfEdges) {
//            Face f = he.getlFace();
//            count.put(f, count.getOrDefault(f, 0) + 1);
//        }

    }

    /* TODO (part 1):
     *   Convert this HalfEdgeMesh into an indexed triangle mesh.
     *   This index triangle mesh will be used by the OpenGL engine
     *   (implemented in c2g2.game.DummyGame) to render the mesh on
     *   the screen.
     *
     *   Note that this HalfEdgeMesh data structure is not intended
     *   to store texture coordinates, so to create a new Mesh instance
     *   one can just pass an array of zeros.
     */
    public Mesh toMesh() {
        int n = vertices.size();
        int faceNum = halfEdges.size() / 3;
        float[] positions = new float[3 * n];
        float[] textCoords = new float[2 * n];
        float[] normals = new float[3 * n];
        int[] indices = new int[faceNum * 3];
        // student code starts here
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 3; j++) {
                positions[3 * i + j] = vertices.get(i).getPos().get(j);
                normals[3 * i + j] = vertices.get(i).getNorm().get(j);
            }
        }
        for (int i = 0; i < faceNum; i++) {
            for (int j = 0; j < 3; j++) {
                indices[3 * i + j] = vertices.indexOf(halfEdges.get(3 * i + j).getNextV());
            }
        }

        // Return a Mesh object instead of null
        return new Mesh(positions, textCoords, normals, indices);
    }

    /* Remove the first vertex from the HalfEdgeMesh.
     * Requires implementation of removeVertex() below.
     */
    public void removeFirstVertex() {
        if (halfEdges.isEmpty()) return;

        Vertex vertex = halfEdges.get(0).getNextV();
        removeVertex(vertex);
    }

    /* Collapse the first edge from the HalfEdgeMesh.
     * Requires implementation of collapseEdge() below.
     */
    public void collapseFirstEdge() {
        if (halfEdges.isEmpty()) return;

        HalfEdge edge = halfEdges.get(0);
        Vertex v = edge.getNextV();
        Vertex u = edge.getFlipE().getNextV();
        Vertex newV = u.getAverage(v);
        newV.getNorm().normalize();
        collapseEdge(edge, newV);
    }

    /* TODO (part 1):
     *   Remove the given vertex, and modify the half-edge data structure
     *   accordingly to ensure the data structure remains valid.
     *
     *   See the specification for the detailed requirement.
     */
    public void removeVertex(Vertex vtx) {
        // Vertex that will inherit all edges adjacent to vtx
        if(vtx.getId()==112){
            System.out.println(vtx);
        }
        Vertex inheritor = vtx.getEdge().getNextV();
        HalfEdge start = vtx.getEdge();
        Face f1 = start.getlFace();
        Face f2 = start.getFlipE().getlFace();

        assert halfEdges.contains(start);
        assert !deletedFace.contains(f1);
        assert !deletedFace.contains(f2);
        deletedFace.add(f1);
        deletedFace.add(f2);

        // student code starts here
        // Remember to update array lists with removed edges and vertices.


        //edges affected
        HalfEdge h1 = null;
        HalfEdge h2 = null;
        HalfEdge h3 = null;
        HalfEdge h4 = null;


        HalfEdge h = f1.getEdge();
        do {
            if (h.getNextV() != vtx && h.getFlipE().getNextV() != vtx) {
                h1 = h.getFlipE();
            }
            if (h.getNextV() == vtx) {
                h2 = h.getFlipE();
            }
            halfEdges.remove(h);
            deletedEdge.add(h);
            h = h.getNextE();
        } while (h != f1.getEdge());

        h = f2.getEdge();
        do {
            if (h.getNextV() != vtx && h.getFlipE().getNextV() != vtx) {
                h3 = h.getFlipE();
            }
            if (h.getFlipE().getNextV() == vtx) {
                h4 = h.getFlipE();
            }
            halfEdges.remove(h);
            deletedEdge.add(h);
            h = h.getNextE();
        } while (h != f2.getEdge());

        //edges affected
        List<HalfEdge> halfEdgeList = new ArrayList<>();
        h = vtx.getEdge();
        do {
            System.out.println(h);
            HalfEdge flip = h.getFlipE();
            halfEdgeList.add(flip);
            h = h.getFlipE().getNextE();
        } while (h != vtx.getEdge());


        assert h1 != null && h2 != null && h3 != null && h4 != null;

        h2.getNextV().setEdge(h1);
        h3.getNextV().setEdge(h4);
        h1.getNextV().setEdge(h3);
        h1.setFlipE(h2);
        h2.setFlipE(h1);
        h3.setFlipE(h4);
        h4.setFlipE(h3);

        for (HalfEdge e : halfEdgeList) {
            e.setNextV(inheritor);
        }

        inheritor.setEdge(h3);

        vertices.remove(vtx);

//        for (HalfEdge he : halfEdges) {
//            if(he.getNextE().getNextV()==he.getFlipE().getNextE().getNextV()){
//                System.out.println(he);
//            }
//            assert he.getNextE().getNextV()!=he.getFlipE().getNextE().getNextV();
//        }
//
//        for (HalfEdge he : halfEdges) {
//            if(deletedEdge.contains(he.getFlipE())){
//                System.out.println(he);
//            }
//            assert !deletedEdge.contains(he.getNextE());
//            assert !deletedEdge.contains(he.getFlipE());
//        }
//
//        for (HalfEdge he : halfEdges) {
//            assert halfEdges.contains(he.getFlipE());
//            assert halfEdges.contains(he.getNextE());
//            assert vertices.contains(he.getNextV());
//        }
//        for (Vertex ve : vertices) {
//            assert halfEdges.contains(ve.getEdge());
//        }
//        for (HalfEdge he : halfEdges) {
//            assert he.getlFace() != f1;
//            assert he.getlFace() != f2;
//        }
//
//        for (HalfEdge he : halfEdges) {
//            assert he.getNextV().getId() != he.getFlipE().getNextV().getId();
//        }
    }

    /* TODO (part 1):
     *   Collapse the given edge into a point.
     *   All edges connected to either end of edge are  connected
     *   to newV after collapse.
     *
     *   See the specification for the detailed requirement.
     */
    public void collapseEdge(HalfEdge edge, Vertex newV) {
        // Hint: collapseHalfTriFan(...) will be useful here.

        // student code starts here
        Vertex v1 = edge.getNextV();
        Vertex v2 = edge.getFlipE().getNextV();

//        if(v1.getId()==15878 || v2.getId()==15878){
//            System.out.println(v1);
//        }
//        for(HalfEdge he : halfEdges){
//            if(he.getNextV().getId()==15878){
//                System.out.println(he);
//            }
//        }
        HalfEdge h1 = edge.getNextE().getFlipE();
        HalfEdge h2 = edge.getNextE().getNextE().getFlipE();
        HalfEdge h3 = edge.getFlipE().getNextE().getFlipE();
        HalfEdge h4 = edge.getFlipE().getNextE().getNextE().getFlipE();

        HalfEdge h = edge;

        do {
            halfEdges.remove(h);
            h = h.getNextE();
        } while (h != edge);
        h = edge.getFlipE();
        do {
            halfEdges.remove(h);
            h = h.getNextE();
        } while (h != edge.getFlipE());


        collapseHalfTriFan(edge, newV);
        collapseHalfTriFan(edge.getFlipE(), newV);

        h1.setFlipE(h2);
        h2.setFlipE(h1);
        h3.setFlipE(h4);
        h4.setFlipE(h3);

        h2.getNextV().setEdge(h1);
        h4.getNextV().setEdge(h3);

        newV.setEdge(h2);
        vertices.remove(v1);
        vertices.remove(v2);
        vertices.add(newV);


        // Remember to update array lists with removed edges and vertices.
        // newV already has a unique id and it will replace one of the
        // removed vertices.
    }


    public ArrayList<Vertex> getVertices() {
        return vertices;
    }

    public ArrayList<HalfEdge> getEdges() {
        return halfEdges;
    }


    /* For a half edge pointing from u to v, redirects all half edges
     * pointing at v (a triangle fan) to point at newV.
     */
    private void collapseHalfTriFan(HalfEdge start, Vertex newV) {
        HalfEdge he = start.getNextE();
        while (he != start.getFlipE()) {
            he.getFlipE().setNextV(newV);
            he = he.getFlipE().getNextE();
        }
    }
}
