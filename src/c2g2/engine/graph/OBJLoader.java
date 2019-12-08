package c2g2.engine.graph;

import java.io.*;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;


public class OBJLoader {
    public static Mesh loadMesh(String fileName) throws Exception {
    	//student code
    	
    	System.out.println("loading "+fileName);
//        List<String> lines = Utils.readAllLines(fileName);
    	List<String> lines = new ArrayList<String>();
    	
    	BufferedReader in = new BufferedReader(new FileReader(fileName));
    	String str;
    	while((str = in.readLine()) != null){
    	    lines.add(str);
    	}
    	in.close();

        System.out.println(lines.size());
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> textures = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Face> faces = new ArrayList<>();

        for (String line : lines) {
            String[] tokens = line.split("\\s+");
            switch (tokens[0]) {
                case "v":
                    // Geometric vertex
                    Vector3f vec3f = new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]));
                    vertices.add(vec3f);
                    break;
                case "vt":
                    // Texture coordinate
                    Vector2f vec2f = new Vector2f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]));
                    textures.add(vec2f);
                    break;
                case "vn":
                    // Vertex normal
                    Vector3f vec3fNorm = new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]));
                    normals.add(vec3fNorm);
                    break;
                case "f":
                    Face face = new Face(tokens[1], tokens[2], tokens[3]);
                    faces.add(face);
                    break;
                default:
                    // Ignore other lines
                    break;
            }
        }
        System.out.println("before");
        System.out.println(vertices.size());
        System.out.println(faces.size());
        Mesh mm = reorderLists(vertices, textures, normals, faces);
        System.out.println("after");
        System.out.println(mm.getPos().length);
        System.out.println(mm.getInds().length);
        OBJLoader.saveMesh(mm,"out.obj");
        return mm;
        
        //float[] positions = null;
        //float[] textCoords = null;
        //float[] norms = null;
        //int[] indices = null;
        //your task is to read data from an .obj file and fill in those arrays.
        //the data in those arrays should use following format.
        //positions[0]=v[0].position.x positions[1]=v[0].position.y positions[2]=v[0].position.z positions[3]=v[1].position.x ...
        //textCoords[0]=v[0].texture_coordinates.x textCoords[1]=v[0].texture_coordinates.y textCoords[2]=v[1].texture_coordinates.x ...
        //norms[0]=v[0].normals.x norms[1]=v[0].normals.y norms[2]=v[0].normals.z norms[3]=v[1].normals.x...
        //indices[0]=face[0].ind[0] indices[1]=face[0].ind[1] indices[2]=face[0].ind[2] indices[3]=face[1].ind[0]...(assuming all the faces are triangle face)
        //return new Mesh(positions, textCoords, norms, indices);
    }



    public static void saveMesh(Mesh mesh, String fileName) throws Exception {
        //student code

        System.out.println("saving "+fileName);
//        List<String> lines = Utils.readAllLines(fileName);
        List<String> lines = new ArrayList<String>();
        float[] pos = mesh.getPos();
        int[] inds = mesh.getInds();
        assert pos.length%3==0;
        System.out.println(pos.length);
        System.out.println(inds.length);
        BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
        for(int i = 0; i < pos.length/3; i ++){
            float x = pos[i*3];
            float y = pos[i*3+1];
            float z = pos[i*3+2];
            out.write("v ");
            out.write(x+ " " + y + " " + z);
            out.newLine();
        }

        for(int i = 0; i < inds.length/3; i++){
            int n1 = inds[i*3]+1;
            int n2 = inds[i*3+1]+1;
            int n3 = inds[i*3+2]+1;
            out.write("f ");
            out.write(n1 + "// " + n2 + "// " + n3 + "//");
            out.newLine();
        }
        out.close();
        return;

        //float[] positions = null;
        //float[] textCoords = null;
        //float[] norms = null;
        //int[] indices = null;
        //your task is to read data from an .obj file and fill in those arrays.
        //the data in those arrays should use following format.
        //positions[0]=v[0].position.x positions[1]=v[0].position.y positions[2]=v[0].position.z positions[3]=v[1].position.x ...
        //textCoords[0]=v[0].texture_coordinates.x textCoords[1]=v[0].texture_coordinates.y textCoords[2]=v[1].texture_coordinates.x ...
        //norms[0]=v[0].normals.x norms[1]=v[0].normals.y norms[2]=v[0].normals.z norms[3]=v[1].normals.x...
        //indices[0]=face[0].ind[0] indices[1]=face[0].ind[1] indices[2]=face[0].ind[2] indices[3]=face[1].ind[0]...(assuming all the faces are triangle face)
        //return new Mesh(positions, textCoords, norms, indices);
    }

    private static Mesh reorderLists(List<Vector3f> posList, List<Vector2f> textCoordList,
            List<Vector3f> normList, List<Face> facesList) {

        List<Integer> indices = new ArrayList<Integer>();
        // Create position array in the order it has been declared
        float[] posArr = new float[posList.size() * 3];
        int i = 0;
        for (Vector3f pos : posList) {
            posArr[i * 3] = pos.x;
            posArr[i * 3 + 1] = pos.y;
            posArr[i * 3 + 2] = pos.z;
            i++;
        }
        float[] textCoordArr = new float[posList.size() * 2];
        float[] normArr = new float[posList.size() * 3];

        for (Face face : facesList) {
            IdxGroup[] faceVertexIndices = face.getFaceVertexIndices();
            for (IdxGroup indValue : faceVertexIndices) {
                processFaceVertex(indValue, textCoordList, normList,
                        indices, textCoordArr, normArr);
            }
        }
        int[] indicesArr = new int[indices.size()];
        indicesArr = indices.stream().mapToInt((Integer v) -> v).toArray();
//        for(float p: posArr){
//            System.out.print(p+"f,");
//        }
//        System.out.println("id:");
//        for(int id: indices){
//        	System.out.print(id+",");
//        }
//        System.out.println("textco: ");
//        for(float p: textCoordArr){
//        	System.out.print(p+"f,");
//        }
//        System.out.println("norm: ");
//        for(float p: normArr){
//        	System.out.print(p+"f,");
//        }
        Mesh mesh = new Mesh(posArr, textCoordArr, normArr, indicesArr);
        return mesh;
    }

    private static void processFaceVertex(IdxGroup indices, List<Vector2f> textCoordList,
            List<Vector3f> normList, List<Integer> indicesList,
            float[] texCoordArr, float[] normArr) {

        // Set index for vertex coordinates
        int posIndex = indices.idxPos;
        indicesList.add(posIndex);

        // Reorder texture coordinates
        if (indices.idxTextCoord >= 0) {
            Vector2f textCoord = textCoordList.get(indices.idxTextCoord);
            texCoordArr[posIndex * 2] = textCoord.x;
            texCoordArr[posIndex * 2 + 1] = 1 - textCoord.y;
        }
        if (indices.idxVecNormal >= 0) {
            // Reorder vectornormals
            Vector3f vecNorm = normList.get(indices.idxVecNormal);
            normArr[posIndex * 3] = vecNorm.x;
            normArr[posIndex * 3 + 1] = vecNorm.y;
            normArr[posIndex * 3 + 2] = vecNorm.z;
        }
    }

    protected static class Face {

        /**
         * List of idxGroup groups for a face triangle (3 vertices per face).
         */
        private IdxGroup[] idxGroups = new IdxGroup[3];

        public Face(String v1, String v2, String v3) {
            idxGroups = new IdxGroup[3];
            // Parse the lines
            idxGroups[0] = parseLine(v1);
            idxGroups[1] = parseLine(v2);
            idxGroups[2] = parseLine(v3);
        }

        private IdxGroup parseLine(String line) {
            IdxGroup idxGroup = new IdxGroup();

            String[] lineTokens = line.split("/");
            int length = lineTokens.length;
            idxGroup.idxPos = Integer.parseInt(lineTokens[0]) - 1;
            if (length > 1) {
                // It can be empty if the obj does not define text coords
                String textCoord = lineTokens[1];
                idxGroup.idxTextCoord = textCoord.length() > 0 ? Integer.parseInt(textCoord) - 1 : IdxGroup.NO_VALUE;
                if (length > 2) {
                    idxGroup.idxVecNormal = Integer.parseInt(lineTokens[2]) - 1;
                }
            }

            return idxGroup;
        }

        public IdxGroup[] getFaceVertexIndices() {
            return idxGroups;
        }
    }

    protected static class IdxGroup {

        public static final int NO_VALUE = -1;

        public int idxPos;

        public int idxTextCoord;

        public int idxVecNormal;

        public IdxGroup() {
            idxPos = NO_VALUE;
            idxTextCoord = NO_VALUE;
            idxVecNormal = NO_VALUE;
        }
    }
}
