package c2g2.game;

import org.joml.Vector2f;
import org.joml.Vector3f;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.glfw.GLFWKeyCallback;

import c2g2.engine.GameItem;
import c2g2.engine.IGameLogic;
import c2g2.engine.MouseInput;
import c2g2.engine.Window;
import c2g2.engine.graph.Camera;
import c2g2.engine.graph.DirectionalLight;
import c2g2.engine.graph.Material;
import c2g2.engine.graph.Mesh;
import c2g2.engine.graph.OBJLoader;
import c2g2.engine.graph.PointLight;
import c2g2.geometry.HalfEdgeMesh;
import c2g2.geometry.MeshSimplifier;

public class DummyGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;

    private static final float SCALE_STEP = 0.01f;

    private static final float TRANSLATE_STEP = 0.01f;

    private static final float ROTATION_STEP = 0.3f;

    private final Vector3f cameraInc;

    private final Renderer renderer;

    private final Camera camera;

    private GameItem[] gameItems;

    private Vector3f ambientLight;

    private PointLight pointLight;

    private DirectionalLight directionalLight;

    private float lightAngle;

    private static final float CAMERA_POS_STEP = 0.05f;

    private int currentObj;

    private String inputFilename;

    private float simplifierRatio;

    private HalfEdgeMesh halfEdgeMesh;

    private MeshSimplifier simplifier;
    
    private boolean wireframe = false;

    public DummyGame(String filename, float ratio) {
        renderer = new Renderer();
        camera = new Camera();
        inputFilename = filename;
        simplifierRatio = ratio;
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        lightAngle = -90;
        currentObj=0;
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);
        float reflectance = 1f;

        Mesh mesh = OBJLoader.loadMesh(inputFilename);  // change mesh file in build.xml
        Material material = new Material(new Vector3f(0.2f, 0.5f, 0.5f), reflectance);
        mesh.setMaterial(material);

        halfEdgeMesh = new HalfEdgeMesh(mesh);
        simplifier = new MeshSimplifier(halfEdgeMesh, simplifierRatio);

        GameItem gameItem = new GameItem(mesh);
        gameItem.setScale(0.5f);
        gameItem.setPosition(0.1f, -0.2f, -2);
        gameItems = new GameItem[]{gameItem};

        ambientLight = new Vector3f(0.8f, 0.8f, 0.8f);
        Vector3f lightColour = new Vector3f(1, 1, 1);
        Vector3f lightPosition = new Vector3f(0, 0, 1);
        float lightIntensity = 1.0f;
        pointLight = new PointLight(lightColour, lightPosition, lightIntensity);
        PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);

        lightPosition = new Vector3f(-1, 0, 0);
        lightColour = new Vector3f(1, 1, 1);
        directionalLight = new DirectionalLight(lightColour, lightPosition, lightIntensity);

        glfwSetKeyCallback(window.getWindowHandle(), new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                } else if (key == GLFW_KEY_N && action == GLFW_RELEASE){
                    //delete vertex
                    halfEdgeMesh.removeFirstVertex();

                    //convert halfedge mesh to trimesh
                    Mesh mm = halfEdgeMesh.toMesh();
                    Material material = gameItems[currentObj].getMesh().getMaterial();
                    mm.setMaterial(material);
                    gameItems[0] = new GameItem(mm);
                    gameItems[0].setScale(0.5f);
                    gameItems[0].setPosition(0.1f, -0.2f, -2);

                } else if (key == GLFW_KEY_M && action == GLFW_RELEASE){
                    // collapse edge
                    halfEdgeMesh.collapseFirstEdge();

                    // convert halfedge mesh to trimesh
                    Mesh mm = halfEdgeMesh.toMesh();
                    Material material = gameItems[currentObj].getMesh().getMaterial();
                    mm.setMaterial(material);
                    gameItems[0] = new GameItem(mm);
                    gameItems[0].setScale(0.5f);
                    gameItems[0].setPosition(0.1f, -0.2f, -2);
                } else if (key == GLFW_KEY_X && action == GLFW_RELEASE) {
                    // simplify mesh
                    simplifier.simplify();

                    // convert halfedge mesh to trimesh
                    Mesh mm = halfEdgeMesh.toMesh();
                    try {
                        OBJLoader.saveMesh(mm,"out.obj");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Material material = gameItems[currentObj].getMesh().getMaterial();
                    mm.setMaterial(material);
                    gameItems[0] = new GameItem(mm);
                    gameItems[0].setScale(0.5f);
                    gameItems[0].setPosition(0.1f, -0.2f, -2);
                } else if (key == GLFW_KEY_Z && action == GLFW_RELEASE) {
                    wireframe = !wireframe;
                    if (wireframe) {
                        glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
                    } else {
                        glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
                    }
                }
            }
        });


    }


    @Override
    public void input(Window window, MouseInput mouseInput) {

    	if(window.isKeyPressed(GLFW_KEY_Q)){
    		//select current object
    		currentObj = currentObj + 1;
    		currentObj = currentObj % gameItems.length;
    	}
    	else if(window.isKeyPressed(GLFW_KEY_W)){
    		//select current object
    		currentObj = currentObj - 1;
    		currentObj = currentObj % gameItems.length;
    	}
    	else if(window.isKeyPressed(GLFW_KEY_E)){
    		//scale object
    		float curr = gameItems[currentObj].getScale();
    		gameItems[currentObj].setScale(curr+SCALE_STEP);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_R)){
    		//scale object
    		float curr = gameItems[currentObj].getScale();
    		gameItems[currentObj].setScale(curr-SCALE_STEP);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_T)){
    		//move object x by step
    		Vector3f curr = gameItems[currentObj].getPosition();
    		gameItems[currentObj].setPosition(curr.x+TRANSLATE_STEP, curr.y, curr.z);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_Y)){
    		//move object x by step
    		Vector3f curr = gameItems[currentObj].getPosition();
    		gameItems[currentObj].setPosition(curr.x-TRANSLATE_STEP, curr.y, curr.z);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_U)){
    		//move object y by step
    		Vector3f curr = gameItems[currentObj].getPosition();
    		gameItems[currentObj].setPosition(curr.x, curr.y+TRANSLATE_STEP, curr.z);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_I)){
    		//move object y by step
    		Vector3f curr = gameItems[currentObj].getPosition();
    		gameItems[currentObj].setPosition(curr.x, curr.y-TRANSLATE_STEP, curr.z);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_O)){
    		//move object z by step
    		Vector3f curr = gameItems[currentObj].getPosition();
    		gameItems[currentObj].setPosition(curr.x, curr.y, curr.z+TRANSLATE_STEP);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_P)){
    		//move object z by step
    		Vector3f curr = gameItems[currentObj].getPosition();
    		gameItems[currentObj].setPosition(curr.x, curr.y, curr.z-TRANSLATE_STEP);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_A)){
    		//rotate object at x axis
    		Vector3f curr = gameItems[currentObj].getRotation();
    		gameItems[currentObj].setRotation(curr.x+ROTATION_STEP, curr.y, curr.z);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_S)){
    		//rotate object at x axis
    		Vector3f curr = gameItems[currentObj].getRotation();
    		gameItems[currentObj].setRotation(curr.x-ROTATION_STEP, curr.y, curr.z);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_D)){
    		//rotate object at x axis
    		Vector3f curr = gameItems[currentObj].getRotation();
    		gameItems[currentObj].setRotation(curr.x, curr.y+ROTATION_STEP, curr.z);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_F)){
    		//rotate object at x axis
    		Vector3f curr = gameItems[currentObj].getRotation();
    		gameItems[currentObj].setRotation(curr.x, curr.y-ROTATION_STEP, curr.z);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_G)){
    		//rotate object at x axis
    		Vector3f curr = gameItems[currentObj].getRotation();
    		gameItems[currentObj].setRotation(curr.x, curr.y, curr.z+ROTATION_STEP);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_H)){
    		//rotate object at x axis
    		Vector3f curr = gameItems[currentObj].getRotation();
    		gameItems[currentObj].setRotation(curr.x, curr.y, curr.z-ROTATION_STEP);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_0)){
    		//rotation by manipulating mesh
    		gameItems[currentObj].getMesh().translateMesh(new Vector3f(0f,0.05f,1f));
    	}
    	else if(window.isKeyPressed(GLFW_KEY_9)){
    		//rotation by manipulating mesh
    		gameItems[currentObj].getMesh().rotateMesh(new Vector3f(1,1,1), 30);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_8)){
    		//rotation by manipulating mesh
    		gameItems[currentObj].getMesh().scaleMesh(1.001f,1.0f,1.0f);
    	}
    	else if(window.isKeyPressed(GLFW_KEY_7)){
    		//rotation by manipulating mesh
    		gameItems[currentObj].getMesh().reflectMesh(new Vector3f(0f,1f,0f), new Vector3f(0f, 1f, 0f));
    	}
    	else if(window.isKeyPressed(GLFW_KEY_1)){
    		//get screenshot
    		renderer.writePNG(window);
    	}
    }

    @Override
    public void update(float interval, MouseInput mouseInput) {
        // Update camera position
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);

        // Update camera based on mouse
        if (mouseInput.isLeftButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            // System.out.println(rotVec);
            Vector3f curr = gameItems[0].getRotation();
            gameItems[0].setRotation(curr.x+ rotVec.x * MOUSE_SENSITIVITY, curr.y+rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        // Update directional light direction, intensity and colour
        lightAngle += 1.1f;

        if (lightAngle > 90) {
            directionalLight.setIntensity(0);
            if (lightAngle >= 90) {
                lightAngle = -90;
            }
        } else if (lightAngle <= -80 || lightAngle >= 80) {
            float factor = 1 - (float) (Math.abs(lightAngle) - 80) / 10.0f;
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }
        double angRad = Math.toRadians(lightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angRad);
        directionalLight.getDirection().y = (float) Math.cos(angRad);
    }

    @Override
    public void render(Window window) {
        renderer.render(window, camera, gameItems, ambientLight, pointLight, directionalLight);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        for (GameItem gameItem : gameItems) {
            gameItem.getMesh().cleanUp();
        }
    }

}
